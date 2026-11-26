package com.xq.web.ai.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 并发锁单元测试。
 *
 * 重点覆盖两个曾经写错的点：
 *   1. 锁必须能被正常释放，否则会员被永久锁死；
 *   2. 超时被接管后，原持有者的 release 不能误删新持有者的锁（否则又变回并发）。
 */
class AiGenerationGuardTest {

    private final AiGenerationGuard guard = new AiGenerationGuard();

    @Test
    @DisplayName("首次获取成功，重复获取失败")
    void acquireOnce() {
        Long first = guard.tryAcquire(1);
        assertNotNull(first, "第一次应获取成功");

        Long second = guard.tryAcquire(1);
        assertNull(second, "同一会员第二次应被拒绝");

        assertEquals(1, guard.runningCount());
    }

    @Test
    @DisplayName("释放后可以再次获取")
    void reacquireAfterRelease() {
        Long t1 = guard.tryAcquire(1);
        assertNotNull(t1);
        guard.release(1, t1);

        Long t2 = guard.tryAcquire(1);
        assertNotNull(t2, "释放后应能重新获取");
    }

    @Test
    @DisplayName("不同会员互不影响")
    void differentMembersIndependent() {
        assertNotNull(guard.tryAcquire(1));
        assertNotNull(guard.tryAcquire(2));
        assertEquals(2, guard.runningCount());
    }

    @Test
    @DisplayName("旧凭证不能删掉新持有者的锁")
    void staleTokenCannotRemoveNewLock() {
        // A 拿到锁
        Long tokenA = guard.tryAcquire(1);
        assertNotNull(tokenA);

        // 模拟 A 被超时接管：清掉后 B 重新获取
        guard.release(1, tokenA);
        Long tokenB = guard.tryAcquire(1);
        assertNotNull(tokenB, "B 应能获取");

        // A 迟到的 finally 用旧凭证释放——必须无效
        guard.release(1, tokenA);

        assertNull(guard.tryAcquire(1),
                "A 的旧凭证不应删掉 B 的锁，此时仍应被拒绝");
    }

    @Test
    @DisplayName("release 传 null 凭证不应报错也不应删锁")
    void releaseWithNullTokenIsNoop() {
        Long t = guard.tryAcquire(1);
        assertNotNull(t);

        guard.release(1, null);

        assertNull(guard.tryAcquire(1), "null 凭证不应释放别人的锁");
        guard.release(1, t);
    }
}
