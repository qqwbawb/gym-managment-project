import router from './router'
import { menuStore } from './store/menu'
import { userStore } from './store/user'

//设置白名单
const whiteList = ['/login']
//全局路由守卫
router.beforeEach(async (to, _from, next) => {
    //获取用户相关数据
    const ustore = userStore()
    //获取菜单的store
    const mstore = menuStore()
    //获取token
    const token = ustore.getToken
    //判断token是否存在
    if (token) {//存在
        if (to.path == '/login' || to.path == '/') {
            next({ path: '/dashboard' })
        } else {
            const menuList = mstore.getMenuList
            if (menuList.length > 0) {
                next()
            } else {
                try {
                    //查询用户信息
                    await ustore.getInfo()
                    //获取菜单信息，动态生成路由
                    await mstore.getMenu(router, { userId: ustore.getUserId, userType: ustore.getUserType })
                    //等待路由全部挂载
                    next({ ...to, replace: true })
                } catch (error) {
                    // token 已失效（后端 Session 丢失/过期）：
                    // 清空本地状态并跳登录页。这里必须先判断目标是不是 /login，
                    // 否则 next('/login') 会再次进入本守卫，形成无限重定向 + 反复弹错。
                    console.error('权限加载失败:', error)
                    ustore.clearUser()
                    mstore.menuList = []
                    if (to.path !== '/login') {
                        next({ path: '/login', replace: true })
                    } else {
                        next()
                    }
                }
            }
        }
    } else {//不存在
        if (whiteList.indexOf(to.path) !== -1) {//存在白名单，直接放行
            next()
        } else {
            ustore.clearUser()
            next({ path: '/login', replace: true })
        }
    }
})