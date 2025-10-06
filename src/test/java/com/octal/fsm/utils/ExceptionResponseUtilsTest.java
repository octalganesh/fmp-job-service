package com.octal.fsm.utils;

import com.octal.fsm.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionResponseUtilsTest {

    @Test
    void responseBadRequest_Success() {
        // Arrange
        String errorMessage = "Test error message";
        Object data = "test data";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/test-context");

        // Act
        ApiResponse result = ExceptionResponseUtils.responseBadRequest(errorMessage, data, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Test error message");
        assertThat(result.getData()).isEqualTo("test data");
        assertThat(result.getStatus()).isEqualTo(String.valueOf(HttpStatus.BAD_REQUEST.value()));
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getPath()).isEqualTo("/test-context");
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    void responseInternalServerError_Success() {
        // Arrange
        String errorMessage = "Internal server error";
        Object data = null;
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/test-context");

        // Act
        ApiResponse result = ExceptionResponseUtils.responseInternalServerError(errorMessage, data, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Internal server error");
        assertThat(result.getData()).isNull();
        assertThat(result.getStatus()).isEqualTo(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getPath()).isEqualTo("/test-context");
        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    void constructor_ThrowsException() {
        // This test verifies that the utility class constructor throws an exception
        // to prevent instantiation (following utility class best practices)
        try {
            // Use reflection to access the private constructor
            java.lang.reflect.Constructor<ExceptionResponseUtils> constructor = 
                ExceptionResponseUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            
            // Act & Assert
            assertThat(constructor.newInstance())
                .isInstanceOf(IllegalStateException.class);
        } catch (Exception e) {
            // Expected behavior - constructor should throw IllegalStateException
            assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("this is utility class");
        }
    }
}