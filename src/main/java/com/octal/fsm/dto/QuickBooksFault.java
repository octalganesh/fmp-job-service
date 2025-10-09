package com.octal.fsm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class QuickBooksFault {
    @JsonProperty("Fault")  // map JSON "Fault" to this field
    private Fault fault;

    @Data
    public static class Fault {
        @JsonProperty("Error") // map JSON "Error" to this field
        private List<QuickBooksError> error;
        private String type;

        @Data
        public static class Error {
            private String Message;
            private String Detail;
            private String code;
        }

        @Data
        public static class QuickBooksError {
            private String Message;
            private String Detail;
            private String code;

            // getters and setters
        }
    }

    private String time;
}
