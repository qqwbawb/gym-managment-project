import axios from "axios";
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from "axios";
import { ElMessage } from 'element-plus';
import { userStore } from '@/store/user';

//错误提示去重：同一个提示短时间内只弹一次，避免并发请求失败时刷屏
let lastErrorMsg = '';
let lastErrorTime = 0;
function showError(msg: string) {
  const now = Date.now();
  if (msg === lastErrorMsg && now - lastErrorTime < 2000) {
    return;
  }
  lastErrorMsg = msg;
  lastErrorTime = now;
  ElMessage.error(msg);
}

//登录失效处理：清理本地状态并跳转登录页
//用标志位保证「并发请求同时 401」时只处理一次，否则两个请求会各弹一次错误并重复跳转
let redirectingToLogin = false;
function handleAuthExpired(msg: string) {
  if (redirectingToLogin) {
    return;
  }
  redirectingToLogin = true;
  showError(msg);
  //必须显式清 store 的 state：localStorage.clear() 只清了持久化数据，
  //内存中的 token 仍在，路由守卫会继续认为「已登录」而陷入重定向循环。
  try {
    userStore().clearUser();
  } catch (e) {
    localStorage.clear();
  }
  //延迟到当前微任务队列结束后再跳转，避免在拦截器里同步操作路由引发死循环
  setTimeout(() => {
    redirectingToLogin = false;
    if (window.location.pathname !== '/login') {
      window.location.href = '/login';
    }
  }, 300);
}

//axios请求配置
const config = {
  //baseURL: 'http://localhost:8089', //真实请求接口的地址，真实上线是一个域名
  baseURL: process.env.BASE_API,
  timeout: 10000,
  withCredentials: true //解决sesson不一致
}

//定义返回值类型
export interface Result<T = any> {
  code: number;
  msg: string;
  data: T;
}

class Http {
  //axios实例
  private instance: AxiosInstance;
  //构造函数里面初始化
  constructor(config: AxiosRequestConfig) {
    this.instance = axios.create(config)
    //定义拦截器
    this.interceptors()
  }
  //拦截器
  private interceptors() {
    //axios发送请求之前的处理
    this.instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
      //在请求头部携带token
      let token = userStore().getToken;
      if (token) {
        config.headers['token'] = token
      }
      return config;
    }, (error: any) => {
      error.data = {}
      error.data.msg = '服务器异常，请联系管理员！'
      return error;
    })
    //axios请求返回之后的处理
    this.instance.interceptors.response.use((res: AxiosResponse) => {
      // 业务层非 200
      if (res.data.code != 200) {
        // code=700 是后端 CustomerAccessDeniedHandler 返回的「无权限访问」，
        // 属于正常的权限拦截（HTTP 状态是 200），需要提示但绝不能让用户掉线
        if (res.data.code === 700) {
          showError(res.data.msg || '无权限访问')
          return Promise.reject(res.data.msg || '无权限访问')
        }
        showError(res.data.msg || '服务器出错!')
        return Promise.reject(res.data.msg || '服务器出错')
      } else {
        return res.data
      }
    }, (error) => {
      error.data = {};
      if (error && error.response) {
        switch (error.response.status) {
          case 400:
            error.data.msg = '错误请求';
            showError(error.data.msg)
            break
          case 401:
            error.data.msg = '登录已失效，请重新登录';
            handleAuthExpired(error.data.msg)
            break
          case 403:
            // 注意：后端 @PreAuthorize 拦截走的是 CustomerAccessDeniedHandler（HTTP 200 + code 700），
            // 正常不会走到这里；真出现 403 也按「登录失效」处理，避免用户卡在空页面
            error.data.msg = '登录已失效，请重新登录';
            handleAuthExpired(error.data.msg)
            break
          case 404:
            error.data.msg = '请求错误,未找到该资源';
            showError(error.data.msg)
            break
          case 405:
            error.data.msg = '请求方法未允许';
            showError(error.data.msg)
            break
          case 408:
            error.data.msg = '请求超时';
            showError(error.data.msg)
            break
          case 500:
            error.data.msg = '服务器端出错';
            showError(error.data.msg)
            break
          case 501:
            error.data.msg = '网络未实现';
            showError(error.data.msg)
            break
          case 502:
            error.data.msg = '网络错误';
            showError(error.data.msg)
            break
          case 503:
            error.data.msg = '服务不可用';
            showError(error.data.msg)
            break
          case 504:
            error.data.msg = '网络超时';
            showError(error.data.msg)
            break
          case 505:
            error.data.msg = 'http版本不支持该请求';
            showError(error.data.msg)
            break
          default:
            error.data.msg = `连接错误${error.response.status}`;
            showError(error.data.msg)
        }
      } else {
        error.data.msg = "连接到服务器失败";
        showError(error.data.msg)
      }
      return Promise.reject(error)
    })
  }

  // restful API的封装
  /* GET 方法 */
  get<T = Result>(url: string, params?: object): Promise<T> {
    return this.instance.get(url, { params }) as Promise<T>
  }
  /* POST 方法 */
  post<T = Result>(url: string, data?: object): Promise<T> {
    return this.instance.post(url, data) as Promise<T>
  }
  /* PUT 方法 */
  put<T = Result>(url: string, data?: object): Promise<T> {
    return this.instance.put(url, data) as Promise<T>
  }
  /* DELETE 方法 */
  delete<T = Result>(url: string): Promise<T> {
    return this.instance.delete(url) as Promise<T>
  }
  //图片上传
  upload<T = Result>(url: string, params?: object): Promise<T> {
    return this.instance.post(url, params, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }) as Promise<T>
  }
}

export default new Http(config)
