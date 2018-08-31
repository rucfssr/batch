package com.batch.component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

/**
 * Exception resolver for the application
 * 
 * @author faizanhussain
 *
 */
@Component
public class RESTExceptionResolver extends DefaultHandlerExceptionResolver {

	public RESTExceptionResolver() {
		setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		logger.error("Exception occurred while processing the request. ", ex);
		return super.resolveException(request, response, handler, ex);
	}

}
