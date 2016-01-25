package com.houlijiang.app.base.api;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.houlijiang.app.base.error.ErrorConst;
import com.houlijiang.app.base.manager.DeployManager;
import com.houlijiang.app.base.utils.AppLog;
import com.houlijiang.common.network.FileWrapper;
import com.houlijiang.common.network.HttpResponseError;
import com.houlijiang.common.network.HttpStringResponse;
import com.houlijiang.common.network.HttpWorker;
import com.houlijiang.common.network.IHttpParams;
import com.houlijiang.common.network.IHttpResponse;
import com.houlijiang.common.network.volley.HttpParams;
import com.houlijiang.common.utils.JsonUtils;
import com.houlijiang.common.utils.ResourceManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by houlijiang on 16/1/22.
 * 
 * api层工具类
 * 
 * 回调都在后台线程中
 */
public class ApiUtils {

    private static final String TAG = ApiUtils.class.getSimpleName();

    /**
     * 过滤url，如果被过滤了，则直接回调，同时返回空，否则返回原始url
     */
    private static String doFilter(UrlFilter filter, String url, String token, IHttpParams params,
        final IApiCallback httpInterface, Object param) {
        String url2 = filter.filter(url, token, params);
        if (TextUtils.isEmpty(url2)) {
            if (httpInterface != null) {
                AppLog.w(TAG, "url:" + url + " is filter, will do nothing");
                ApiResultModel model = new ApiResultModel();
                model.code = ErrorConst.ERROR_CODE_RUNTIME_ILLEGAL_ACCESS;
                httpInterface.onRequestCompleted(model, param);
            }
            return null;
        }
        return url;
    }

    /**
     * 添加进资源管理类中，当tag被回收后，自动cancel网络请求
     * 
     * @param tag 传入的生命周期相关对象
     * @return 网络请求关联的对象
     */
    private static int addToResourceManager(Object tag) {
        int hash = tag.hashCode();
        ResourceManager.getInstance().addRequest(tag, new ApiCancel(hash));
        return hash;
    }

    /**
     * 进行get请求
     *
     * @param tag 请求相关tag，删除时用
     * @param url 地址
     * @param params 请求
     * @param param 用户自定参数，回调时传回
     * @param httpInterface 回调
     */
    public static void doGet(Object tag, String url, String token, IHttpParams params, Object param,
        final IApiCallback httpInterface, UrlFilter filter) {
        if (params == null) {
            params = HttpWorker.createHttpParams();
        }

        String newUrl = url;
        if (filter != null) {
            newUrl = doFilter(filter, newUrl, token, params, httpInterface, param);
            if (TextUtils.isEmpty(newUrl)) {
                return;
            }
        }
        createDefaultParams(newUrl, token, params);
        int h = addToResourceManager(tag);
        HttpWorker.get(h, newUrl, params, HttpStringResponse.class, new InnerBaseApiListener(httpInterface), param);
    }

    /**
     * 进行get请求
     *
     * @param tag 请求相关tag，删除时用
     * @param url 地址
     * @param params 请求
     * @param httpInterface 回调
     */
    public static void doGet(Object tag, String url, String token, IHttpParams params, final IApiCallback httpInterface) {
        doGet(tag, url, token, params, null, httpInterface, null);
    }

    /**
     * 进行post请求
     *
     * @param tag 请求相关tag，删除时用
     * @param url 地址
     * @param headers 自定义请求头，用于给服务器一些调试参数
     * @param params 参数
     * @param param 用户自定参数，回调时传回
     * @param httpInterface 回调
     */
    public static void doPost(Object tag, String url, String token, String contentType, Map<String, String> headers,
        IHttpParams params, Object param, final IApiCallback httpInterface, UrlFilter filter) {
        if (params == null) {
            params = HttpWorker.createHttpParams();
        }
        String newUrl = url;
        if (filter != null) {
            newUrl = doFilter(filter, newUrl, token, params, httpInterface, param);
            if (TextUtils.isEmpty(newUrl)) {
                return;
            }
        }
        createDefaultParams(newUrl, token, params);
        int h = addToResourceManager(tag);
        HttpWorker.post(h, newUrl, params, contentType, headers, HttpStringResponse.class, new InnerBaseApiListener(
            httpInterface), param);
    }

    /**
     * 进行post请求
     *
     * @param tag 请求相关tag，删除时用
     * @param url 地址
     * @param headers 自定义请求头，用于给服务器一些调试参数
     * @param params 参数
     * @param param 用户自定参数，回调时传回
     * @param httpInterface 回调
     */
    public static void doPost(Object tag, String url, String token, Map<String, String> headers, IHttpParams params,
        Object param, final IApiCallback httpInterface) {
        doPost(tag, url, token, null, headers, params, param, httpInterface, null);
    }

    /**
     * 进行post请求
     *
     * @param tag 请求相关tag，删除时用
     * @param url 地址
     * @param params 参数
     * @param httpInterface 回调
     */
    public static void doPost(Object tag, String url, String token, IHttpParams params, final IApiCallback httpInterface) {
        doPost(tag, url, token, null, null, params, null, httpInterface, null);
    }

    /**
     * 进行post请求
     *
     * @param tag 请求相关tag，删除时用
     * @param url 地址
     * @param params 参数
     * @param httpInterface 回调
     * @param filter url最后的处理
     */
    public static void doPost(Object tag, String url, String token, IHttpParams params,
        final IApiCallback httpInterface, final UrlFilter filter) {
        doPost(tag, url, token, null, null, params, null, httpInterface, filter);
    }

    /**
     * 进行post请求
     *
     * @param tag 请求相关tag，删除时用
     * @param url 地址
     * @param params 参数
     * @param param 用户自定参数，回调时传回
     * @param httpInterface 回调
     */
    public static void doPost(Object tag, String url, String token, IHttpParams params, Object param,
        IApiCallback httpInterface) {
        doPost(tag, url, token, null, null, params, param, httpInterface, null);
    }

    /**
     * 上传文件
     *
     * @param tag 请求相关tag，删除时用
     * @param url 地址
     * @param params 参数
     * @param httpInterface 回调
     */
    public static void upload(Object tag, String url, String token, File file, String contentType,
        String customFileName, IHttpParams params, final IApiCallback httpInterface) {
        upload(tag, url, null, file, contentType, customFileName, token, params, null, httpInterface);
    }

    /**
     * 上传文件
     *
     * @param tag 请求相关tag，删除时用
     * @param url 地址
     * @param params 参数
     * @param param 用户自定参数，回调时传回
     * @param httpInterface 回调
     */
    public static void upload(Object tag, String url, Map<String, String> headers, File file, String contentType,
        String customFileName, String token, IHttpParams params, Object param, final IApiCallback httpInterface) {
        if (params == null) {
            params = HttpWorker.createHttpParams();
        }
        Map<String, FileWrapper> files = new HashMap<>();
        files.put("attachment", new FileWrapper(file, contentType, customFileName));
        createDefaultParams(url, token, params);
        int h = addToResourceManager(tag);
        HttpWorker.upload(h, url, headers, files, params, HttpStringResponse.class, new InnerBaseApiListener(
            httpInterface), param);
    }

    /**
     * 下载文件
     *
     * @param tag 请求相关tag，删除时用
     * @param url url
     * @param file 下载文件存放位置
     * @param params 参数
     * @param httpInterface 回调
     */
    public static void download(Object tag, String url, String token, File file, IHttpParams params,
        final IDownloadCallback httpInterface) {
        download(tag, url, token, file, params, null, httpInterface);
    }

    /**
     * 下载文件
     *
     * @param tag 请求相关tag，删除时用
     * @param url url
     * @param file 下载文件存放位置
     * @param params 参数
     * @param param 用户自定参数，回调时传回
     * @param httpInterface 回调
     */
    public static void download(Object tag, String url, String token, File file, IHttpParams params, Object param,
        final IDownloadCallback httpInterface) {
        if (params == null) {
            params = new HttpParams();
        }
        createDefaultParams(url, token, params);
        int h = addToResourceManager(tag);
        HttpWorker.download(h, url, null, file, params, new IHttpResponse<File>() {
            @Override
            public void onSuccess(@NonNull File file, Object param) {
                if (httpInterface != null) {
                    ApiResultModel model = new ApiResultModel();
                    model.code = ErrorConst.ERROR_CODE_SUCCESS;
                    httpInterface.onRequestCompleted(model, param);
                }
            }

            @Override
            public void onFailed(@NonNull HttpResponseError error, Object param) {
                if (httpInterface != null) {
                    ApiResultModel apiResult = convertApiError(error);
                    httpInterface.onRequestCompleted(apiResult, param);
                }
            }

            @Override
            public void onProgress(long donebytes, long totalbytes, Object param) {
                if (httpInterface != null) {
                    httpInterface.onDownloadProgress(donebytes, totalbytes);
                }
            }
        }, param);

    }

    /**
     * 获取默认参数
     */
    public static IHttpParams createDefaultParams(String url, String authToken, IHttpParams originParams) {
        IHttpParams params = originParams;
        if (params == null) {
            params = HttpWorker.createHttpParams();
        }
        if (!TextUtils.isEmpty(DeployManager.getVersion())) {
            params.put("version", DeployManager.getVersion());
        }
        if (!TextUtils.isEmpty(DeployManager.getApiVersion())) {
            params.put("api", DeployManager.getApiVersion());
        }
        if (!TextUtils.isEmpty(DeployManager.getPlatform())) {
            params.put("platform", DeployManager.getPlatform());
        }
        if (!TextUtils.isEmpty(DeployManager.getOs())) {
            params.put("os", DeployManager.getOs());
        }
        if (!TextUtils.isEmpty(DeployManager.getImei())) {
            params.put("l_imei", DeployManager.getImei());
        }
        if (!TextUtils.isEmpty(DeployManager.getMac())) {
            params.put("l_mac", DeployManager.getMac());
        }
        if (!TextUtils.isEmpty(DeployManager.getUuid())) {
            params.put("uuid", DeployManager.getUuid());
        }
        if (!TextUtils.isEmpty(DeployManager.getChannel())) {
            params.put("channel", DeployManager.getChannel());
        }
        // signUrl(params, url, authToken);

        params.put("logId", UUID.randomUUID().toString());
        return params;
    }

    /**
     * 将网络层错误转成通用错误
     * 
     * @param error 网络层错误
     * @return 通用错误model
     */
    private static ApiResultModel convertApiError(HttpResponseError error) {
        ApiResultModel apiResult = new ApiResultModel();
        switch (error.getCode()) {
            case HttpResponseError.ERROR_AUTH: {
                apiResult.code = ErrorConst.ERROR_UNKNOWN;
            }
        }
        apiResult.message = error.getReason();
        return apiResult;
    }

    /**
     * 统一处理网络层回调，检查基本数据结构，看是否符合和服务器的约定，具体的解析交由业务层处理
     */
    private final static class InnerBaseApiListener implements IHttpResponse<HttpStringResponse> {

        private static final String TAG = InnerBaseApiListener.class.getSimpleName();
        private static final JsonParser jsonParser = new JsonParser();

        private IApiCallback mListener;

        private InnerBaseApiListener(IApiCallback listener) {
            this.mListener = listener;
        }

        /**
         * 处理网络返回结果，如果成功则尝试解析基本数据结构，并判断结构是否正确
         */
        @Override
        public void onSuccess(@NonNull HttpStringResponse o, Object param) {
            if (mListener == null) {
                return;
            }

            ApiResultModel apiResult = new ApiResultModel();
            try {
                BaseApiModel model = new BaseApiModel();
                JsonObject jsonObject = jsonParser.parse(o.data).getAsJsonObject();

                if (jsonObject.has("code")) {
                    if (jsonObject.get("code") instanceof JsonNull) {
                        model.code = ErrorConst.ERROR_CODE_FAIL;
                    } else {
                        model.code = jsonObject.get("code").getAsInt();
                    }
                }

                if (jsonObject.has("msg")) {
                    if (jsonObject.get("msg") instanceof JsonNull) {
                        model.msg = null;
                    } else {
                        model.msg = jsonObject.get("msg").getAsString();
                    }
                }

                if (jsonObject.has("pageDto")) {
                    if (jsonObject.get("pageDto") instanceof JsonNull) {
                        model.pageDto = null;
                    } else {
                        model.pageDto = JsonUtils.parseString(jsonObject.get("pageDto").toString(), BaseApiModel.PageDTO.class);
                    }
                }

                if (jsonObject.has("data")) {
                    if (jsonObject.get("data") instanceof JsonNull) {
                        model.data = "";
                    } else {
                        model.data = jsonObject.get("data").toString();
                    }
                }

                if (model.pageDto != null) {
                    apiResult.code = model.code;
                    apiResult.message = model.msg;
                    if (model.data == null) {
                        apiResult.result = "";
                    } else {
                        apiResult.result = model.data;
                    }
                    apiResult.pageInfo = new ApiResultModel.PageInfo();
                    apiResult.pageInfo.currentPage = model.pageDto.pageNum;
                    apiResult.pageInfo.currentPageCount = model.pageDto.curPageCount;
                    apiResult.pageInfo.pageSize = model.pageDto.pageSize;
                    apiResult.pageInfo.totalCount = model.pageDto.count;
                    mListener.onRequestCompleted(apiResult, param);
                } else {
                    apiResult.code = model.code;
                    apiResult.message = model.msg;
                    if (model.data == null) {
                        apiResult.result = "";
                    } else {
                        apiResult.result = model.data;
                    }
                    mListener.onRequestCompleted(apiResult, param);
                }
            } catch (JsonSyntaxException e) {
                AppLog.e(TAG, "parse json error, e:" + e.getLocalizedMessage());
                apiResult.code = ErrorConst.ERROR_CODE_JSON_PARSE;
                apiResult.result = o.data;
                mListener.onRequestCompleted(apiResult, param);
            }
        }

        @Override
        public void onFailed(@NonNull HttpResponseError error, Object param) {
            if (mListener == null) {
                return;
            }
            ApiResultModel apiResult = convertApiError(error);
            mListener.onRequestCompleted(apiResult, param);
        }

        @Override
        public void onProgress(long donebytes, long totalbytes, Object param) {
        }
    }

    /**
     * 网络请求取消类的包装，使用ResourceManager来管理网络请求的自动取消
     */
    private static class ApiCancel implements ResourceManager.Cancelable {

        private int hashCode;

        public ApiCancel(int hash) {
            hashCode = hash;
        }

        @Override
        public boolean cancel() {
            try {
                HttpWorker.cancel(hashCode);
            } catch (Exception e) {
                AppLog.e(TAG, "cancel http call e:" + e.getLocalizedMessage());
            }
            return false;
        }
    }

    public interface UrlFilter {
        /**
         * 对url进行再处理
         *
         * @param url 原始url
         * @param token 授权码
         * @param params 参数
         * @return 修改过的url，如果返回空则表示被过滤了以特定异常回调
         */
        String filter(String url, String token, IHttpParams params);
    }

}
