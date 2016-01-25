package com.houlijiang.common.service;

import com.houlijiang.app.base.api.ApiResultModel;
import com.houlijiang.app.base.api.IApiCallback;
import com.houlijiang.app.base.service.BaseDataService;
import com.houlijiang.app.base.service.IDataServiceCallback;
import com.houlijiang.common.api.AuthApi;
import com.houlijiang.common.model.LoginModel;

/**
 * Created by houlijiang on 16/1/23.
 */
public class AuthDataService extends BaseDataService {

    public static final String SERVICE_KEY = AuthDataService.class.getSimpleName();

    private AuthApi mAuthApi;

    public AuthDataService() {
        super();
        this.mAuthApi = new AuthApi();
    }

    /**
     * 获取添加课程和添加老师url
     *
     * @param origin 生命周期控制对象
     * @param callback 回调
     * @param param 回调参数
     * @return 网络请求引用
     */
    public void login(Object origin, String name, String passwd, final IDataServiceCallback<LoginModel> callback,
        final Object param) {

        mAuthApi.login(origin, name, passwd, new IApiCallback() {
            @Override
            public void onRequestCompleted(ApiResultModel result, Object param) {
                processApiResult(result, LoginModel.class, callback, param);
            }
        }, param);
    }
}
