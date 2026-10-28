import type { Directive } from 'vue'
import { userStore } from '@/store/user'

/**
 * 按钮级权限指令：v-permission="['sys:orderList:down']"
 *
 * 用法：模板中给按钮添加该指令并传入所需的权限码数组，
 * 当前登录用户（store.codeList，登录后由 /api/login/getInfo 填充）
 * 若一个权限码都没有，则该元素（按钮）直接从 DOM 中移除，不渲染。
 *
 * 注意：这层只是前端体验层的按钮显隐控制，
 * 真正的权限校验以后端 @PreAuthorize 为准，切勿把安全寄托在前端。
 */
const permission: Directive = {
    mounted(el: HTMLElement, binding) {
        const required = binding.value as string[] | undefined
        if (!required || required.length === 0) {
            return
        }
        const store = userStore()
        const hasPermission = required.some(code => store.codeList.includes(code))
        if (!hasPermission) {
            el.parentNode && el.parentNode.removeChild(el)
        }
    }
}

export default permission
