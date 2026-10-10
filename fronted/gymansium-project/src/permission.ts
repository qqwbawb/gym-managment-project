import router from './router'
import { menuStore } from './store/menu'
import { userStore } from './store/user'

//设置白名单
const whiteList = ['/login']
//全局路由守卫
router.beforeEach(async (to, from, next) => {
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
                    console.error('权限加载失败:', error)
                    localStorage.clear()
                    next({ path: '/login' })
                }
            }
        }
    } else {//不存在
        if (whiteList.indexOf(to.path) !== -1) {//存在白名单，直接放行
            next()
        } else {
            localStorage.clear()
            next({ path: '/login' })
        }
    }
})