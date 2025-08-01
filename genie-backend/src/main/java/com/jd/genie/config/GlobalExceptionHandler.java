package com.jd.genie.config;

import com.jd.genie.agent.exception.AgentException;
import com.jd.genie.agent.exception.OrchestratorException;
import com.jd.genie.agent.tool.mcp.McpIntegrationService.McpToolException;
import com.jd.genie.agent.workflow.engine.WorkflowEngine.WorkflowExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常，提供标准化的错误响应
 * 
 * @author Stock Agent Team
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理智能体异常
     */
    @ExceptionHandler(AgentException.class)
    public ResponseEntity<ErrorResponse> handleAgentException(AgentException ex, HttpServletRequest request) {
        logger.error("Agent exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Agent Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .code("AGENT_ERROR")
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理协调器异常
     */
    @ExceptionHandler(OrchestratorException.class)
    public ResponseEntity<ErrorResponse> handleOrchestratorException(OrchestratorException ex, HttpServletRequest request) {
        logger.error("Orchestrator exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Orchestrator Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .code("ORCHESTRATOR_ERROR")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理工作流执行异常
     */
    @ExceptionHandler(WorkflowExecutionException.class)
    public ResponseEntity<ErrorResponse> handleWorkflowExecutionException(WorkflowExecutionException ex, HttpServletRequest request) {
        logger.error("Workflow execution exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Workflow Execution Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .code("WORKFLOW_EXECUTION_ERROR")
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理MCP工具异常
     */
    @ExceptionHandler(McpToolException.class)
    public ResponseEntity<ErrorResponse> handleMcpToolException(McpToolException ex, HttpServletRequest request) {
        logger.error("MCP tool exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("MCP Tool Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .code("MCP_TOOL_ERROR")
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理实体未找到异常
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        logger.warn("Entity not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Resource Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .code("RESOURCE_NOT_FOUND")
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.warn("Validation failed: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("请求参数验证失败")
                .path(request.getRequestURI())
                .code("VALIDATION_ERROR")
                .details(fieldErrors)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        logger.warn("Bind exception: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Parameter Binding Failed")
                .message("参数绑定失败")
                .path(request.getRequestURI())
                .code("BIND_ERROR")
                .details(fieldErrors)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        logger.warn("Constraint violation: {}", ex.getMessage());
        
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        Map<String, String> fieldErrors = violations.stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message("约束验证失败")
                .path(request.getRequestURI())
                .code("CONSTRAINT_VIOLATION")
                .details(fieldErrors)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.warn("Missing request parameter: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Missing Parameter")
                .message(String.format("缺少必需的请求参数: %s", ex.getParameterName()))
                .path(request.getRequestURI())
                .code("MISSING_PARAMETER")
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logger.warn("Method argument type mismatch: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Type Mismatch")
                .message(String.format("参数类型不匹配: %s", ex.getName()))
                .path(request.getRequestURI())
                .code("TYPE_MISMATCH")
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.warn("HTTP message not readable: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Message Not Readable")
                .message("请求消息格式错误")
                .path(request.getRequestURI())
                .code("MESSAGE_NOT_READABLE")
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理不支持的HTTP方法异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        logger.warn("HTTP method not supported: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message(String.format("不支持的HTTP方法: %s", ex.getMethod()))
                .path(request.getRequestURI())
                .code("METHOD_NOT_ALLOWED")
                .build();
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * 处理不支持的媒体类型异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        logger.warn("HTTP media type not supported: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error("Unsupported Media Type")
                .message("不支持的媒体类型")
                .path(request.getRequestURI())
                .code("UNSUPPORTED_MEDIA_TYPE")
                .build();
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        logger.warn("Max upload size exceeded: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error("File Too Large")
                .message("上传文件大小超过限制")
                .path(request.getRequestURI())
                .code("FILE_TOO_LARGE")
                .build();
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    /**
     * 处理找不到处理器异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        logger.warn("No handler found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("请求的资源不存在")
                .path(request.getRequestURI())
                .code("NOT_FOUND")
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 处理数据访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, HttpServletRequest request) {
        logger.error("Data access exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Database Error")
                .message("数据库操作失败")
                .path(request.getRequestURI())
                .code("DATABASE_ERROR")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理数据完整性违反异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.error("Data integrity violation: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message("数据完整性约束违反")
                .path(request.getRequestURI())
                .code("DATA_INTEGRITY_VIOLATION")
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * 处理超时异常
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeoutException(TimeoutException ex, HttpServletRequest request) {
        logger.error("Timeout exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.REQUEST_TIMEOUT.value())
                .error("Request Timeout")
                .message("请求超时")
                .path(request.getRequestURI())
                .code("REQUEST_TIMEOUT")
                .build();
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Illegal Argument")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .code("ILLEGAL_ARGUMENT")
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        logger.error("Illegal state: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Illegal State")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .code("ILLEGAL_STATE")
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        logger.error("Runtime exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Runtime Error")
                .message("系统运行时错误")
                .path(request.getRequestURI())
                .code("RUNTIME_ERROR")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("服务器内部错误")
                .path(request.getRequestURI())
                .code("INTERNAL_ERROR")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 错误响应模型
     */
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private String code;
        private Object details;
        private String traceId;

        // 构造器
        private ErrorResponse() {}

        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public Object getDetails() { return details; }
        public void setDetails(Object details) { this.details = details; }

        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }

        /**
         * 错误响应构建器
         */
        public static class ErrorResponseBuilder {
            private final ErrorResponse errorResponse = new ErrorResponse();

            public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
                errorResponse.setTimestamp(timestamp);
                return this;
            }

            public ErrorResponseBuilder status(int status) {
                errorResponse.setStatus(status);
                return this;
            }

            public ErrorResponseBuilder error(String error) {
                errorResponse.setError(error);
                return this;
            }

            public ErrorResponseBuilder message(String message) {
                errorResponse.setMessage(message);
                return this;
            }

            public ErrorResponseBuilder path(String path) {
                errorResponse.setPath(path);
                return this;
            }

            public ErrorResponseBuilder code(String code) {
                errorResponse.setCode(code);
                return this;
            }

            public ErrorResponseBuilder details(Object details) {
                errorResponse.setDetails(details);
                return this;
            }

            public ErrorResponseBuilder traceId(String traceId) {
                errorResponse.setTraceId(traceId);
                return this;
            }

            public ErrorResponse build() {
                return errorResponse;
            }
        }
    }
}