package com.xq.web.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 生成任务并发保护：同一个会员同一时刻只允许跑一个生成任务。
 *
 * 为什么需要：
 *   生成一次要 20-120 秒、消耗真实 token。用户连点几次「发送」，
 *   就会并发发出多个请求，各烧一遍 token，结果还会互相覆盖（存多条计划）。
 *
 * 为什么要有「超时兜底」：
 *   只有 acquire/release 是不够的——如果生成线程因为任何原因没能走到 release，
 *   这个会员就再也无法生成，只能重启服务。所以给每把锁记持有时间，
 *   超过 MAX_HOLD_MS 视为失效，允许接管。
 *
 * 为什么凭证用「原子递增序号」而不是时间戳：
 *   时间戳只有毫秒精度，同一毫秒内先后获取的两把锁会拿到相同的凭证，
 *   导致旧持有者的 release 误删新持有者的锁，并发保护直接失效。
 *   （这个问题是被 AiGenerationGuardTest 抓出来的。）
 *
 * 局限性（如实说明）：
 *   这是【单机内存锁】，多实例部署时各实例互相看不见。
 *   分布式场景需要换 Redis（SETNX + TTL）。
 */
@Slf4j
@Component
public class AiGenerationGuard {

    /** 锁的最长持有时间：4 分钟。超过即视为异常残留，允许接管 */
    private static final long MAX_HOLD_MS = 4 * 60 * 1000L;

    /** 锁记录：持有时刻 + 唯一凭证 */
    private static final class Holder {
        final long since;
        final long token;

        Holder(long since, long token) {
            this.since = since;
            this.token = token;
        }
    }

    private final Map<Integer, Holder> running = new ConcurrentHashMap<>();

    /** 全局递增凭证，保证任意两次获取得到的 token 一定不同 */
    private final AtomicLong tokenSeq = new AtomicLong();

    /**
     * 尝试占用生成名额。
     *
     * @return 成功返回凭证（必须原样传给 release）；失败返回 null
     */
    public Long tryAcquire(Integer memberId) {
        final long now = System.currentTimeMillis();
        final long newToken = tokenSeq.incrementAndGet();
        final Long[] acquired = {null};

        running.compute(memberId, (id, holder) -> {
            if (holder == null) {
                acquired[0] = newToken;
                return new Holder(now, newToken);
            }
            if (now - holder.since > MAX_HOLD_MS) {
                // 上一次任务明显卡死/异常退出，强制接管，避免会员被永久锁死
                log.warn("会员 {} 的生成锁已持有 {} ms 超过上限，强制接管",
                        memberId, now - holder.since);
                acquired[0] = newToken;
                return new Holder(now, newToken);
            }
            return holder;
        });

        if (acquired[0] == null) {
            log.warn("会员 {} 已有生成任务在执行，拒绝重复提交", memberId);
        }
        return acquired[0];
    }

    /**
     * 释放名额。必须放在 finally 里。
     *
     * 条件删除：只有凭证与当前锁一致才删除。
     * 这样 A 因超时被 B 接管后，A 结束时不会误删 B 的锁。
     */
    public void release(Integer memberId, Long token) {
        if (token == null) {
            return;
        }
        running.computeIfPresent(memberId, (id, holder) ->
                holder.token == token ? null : holder);
    }

    /**
     * 当前正在生成的数量（供监控/排查使用）。
     */
    public int runningCount() {
        return running.size();
    }
}
