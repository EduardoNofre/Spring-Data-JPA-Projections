package com.api.spring.projections.app.estudo.exceptions;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.LocalDateTime;

import javax.annotation.Resource;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.api.spring.projections.app.estudo.enums.protocoloInternalErrorCodesEnum;
import com.api.spring.projections.app.estudo.model.CustomErrorResponse;
import com.api.spring.projections.app.estudo.model.ErrorDetail;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@Resource
	private MessageSource messageSource;
	

    @ExceptionHandler(Exception.class)
    private ResponseEntity<Object> handleGeneral(Exception e, WebRequest request) {
        String message = "";
        if (e.getClass().isAssignableFrom(UndeclaredThrowableException.class)) {

            UndeclaredThrowableException exception = (UndeclaredThrowableException) e;
            Class<? extends Throwable> exceptionClass = exception.getUndeclaredThrowable().getClass();

            if (exceptionClass.isAssignableFrom(ApiException.class)) {
                return handleCliente((ApiException) exception.getUndeclaredThrowable(), request);
            }
            if (CodigoErroException.class.isAssignableFrom(exceptionClass)) {
                return handleCodigoDeErro((CodigoErroException) exception.getUndeclaredThrowable(), request);
            }

            message = messageSource.getMessage("error.server", new Object[]{exception.getUndeclaredThrowable().getMessage()}, null);
        } else {
            message = messageSource.getMessage("error.server", new Object[]{e.getMessage()}, null);
        }

        CustomErrorResponse error = new CustomErrorResponse();
        error.setError(message);
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setTimestamp(LocalDateTime.now());
        error.setCodigo(protocoloInternalErrorCodesEnum.E500000.getErrorcode());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.error("Erro interno: {}", e.getMessage(), e);

        return handleExceptionInternal(e, error, headers, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler({CodigoErroException.class})
    private ResponseEntity<Object> handleCodigoDeErro(Exception e, WebRequest request) {
    	CustomErrorResponse error = new CustomErrorResponse();
        CodigoErroException codigoDeErroException = (CodigoErroException) e;

        String codigoHttp = (codigoDeErroException.getErrorcode().getErrorcode().split("\\.")[0]);
        int httpStatus = Integer.valueOf(codigoHttp);
        error.setError(e.getMessage());
        error.setStatus(httpStatus);
        error.setTimestamp(LocalDateTime.now());
        error.setCodigo(codigoDeErroException.getErrorcode().getErrorcode());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.debug("Erro genérico: {}", e.getMessage(), e);

        return handleExceptionInternal(e, error, headers, HttpStatus.valueOf(httpStatus), request);
    }

    @ExceptionHandler({ApiException.class})
    private ResponseEntity<Object> handleCliente(Exception e, WebRequest request) {
        String message = messageSource.getMessage("error.cliente", new Object[]{e.getMessage()}, null);

        ErrorDetail errorDetail = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            errorDetail = objectMapper.readValue(message, ErrorDetail.class);
        } catch (IOException io) {
            log.debug("Erro com a transformação de Objeto x JSON. ", io);
        }

        CustomErrorResponse error = new CustomErrorResponse();
        error.setError(errorDetail == null ? message : errorDetail.getErrorDetailFuncionario().getCode() + " - " + errorDetail.getErrorDetailFuncionario().getDescription());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setTimestamp(LocalDateTime.now());
        error.setCodigo("460." + String.format("%03d", errorDetail.getErrorDetailFuncionario().getCode()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.debug("Erro no client da cliente. {}", e.getMessage(), e);

        return handleExceptionInternal(e, error, headers, HttpStatus.BAD_REQUEST, request);
    }
}
