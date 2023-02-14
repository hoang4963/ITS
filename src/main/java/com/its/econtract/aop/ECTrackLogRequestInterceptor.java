package com.its.econtract.aop;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ECTrackLogRequestInterceptor implements HandlerInterceptor {

    private static final String START_ATTR = ECTrackLogRequestInterceptor.class.getName() + ".startTime";
    private static final String HANDLING_ATTR = ECTrackLogRequestInterceptor.class.getName() + ".handlingTime";
    private static final long DEFAULT_WARN_NANOS;
    private long warnHandlingNanos;

    public ECTrackLogRequestInterceptor() {
        this.warnHandlingNanos = DEFAULT_WARN_NANOS;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        } else {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method m = handlerMethod.getMethod();
            log.trace("[START CONTROLLER] {}.{}({})", new Object[]{m.getDeclaringClass().getSimpleName(), m.getName(), buildMethodParams(handlerMethod)});
            long startTime = System.nanoTime();
            request.setAttribute(START_ATTR, startTime);
            return true;
        }
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (handler instanceof HandlerMethod) {
            long startTime = 0L;
            if (request.getAttribute(START_ATTR) != null) {
                startTime = (Long)request.getAttribute(START_ATTR);
            }

            long handlingTime = System.nanoTime() - startTime;
            request.removeAttribute(START_ATTR);
            request.setAttribute(HANDLING_ATTR, handlingTime);
            String formattedHandlingTime = String.format("%1$,3d", handlingTime);
            boolean isWarnHandling = handlingTime > this.warnHandlingNanos;
            if (this.isEnabledLogLevel(isWarnHandling)) {
                HandlerMethod handlerMethod = (HandlerMethod)handler;
                Method m = handlerMethod.getMethod();
                Object view = null;
                Map<String, Object> model = null;
                if (modelAndView != null) {
                    view = modelAndView.getView();
                    model = modelAndView.getModel();
                    if (view == null) {
                        view = modelAndView.getViewName();
                    }
                }

                log.trace("[END CONTROLLER  ] {}.{}({})-> view={}, model={}", new Object[]{m.getDeclaringClass().getSimpleName(), m.getName(), buildMethodParams(handlerMethod), view, model});
                String handlingTimeMessage = "[HANDLING TIME   ] {}.{}({})-> {} ns";
                if (isWarnHandling) {
                    log.warn(handlingTimeMessage + " > {}", new Object[]{m.getDeclaringClass().getSimpleName(), m.getName(), buildMethodParams(handlerMethod), formattedHandlingTime, this.warnHandlingNanos});
                } else {
                    log.trace(handlingTimeMessage, new Object[]{m.getDeclaringClass().getSimpleName(), m.getName(), buildMethodParams(handlerMethod), formattedHandlingTime});
                }
            }
        }
    }

    private boolean isEnabledLogLevel(boolean isWarnHandling) {
        if (isWarnHandling) {
            return true;
        } else return log.isTraceEnabled();
    }

    protected static String buildMethodParams(HandlerMethod handlerMethod) {
        MethodParameter[] params = handlerMethod.getMethodParameters();
        List<String> lst = new ArrayList(params.length);
        MethodParameter[] methodParameters = params;
        int paramLength = params.length;

        for(int i = 0; i < paramLength; ++i) {
            MethodParameter p = methodParameters[i];
            lst.add(p.getParameterType().getSimpleName());
        }

        return StringUtils.collectionToCommaDelimitedString(lst);
    }

    public void setWarnHandlingNanos(long warnHandlingNanos) {
        this.warnHandlingNanos = warnHandlingNanos;
    }

    static {
        DEFAULT_WARN_NANOS = TimeUnit.SECONDS.toNanos(3L);
    }
}
