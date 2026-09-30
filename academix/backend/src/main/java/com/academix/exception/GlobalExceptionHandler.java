package com.academix.exception;
import org.springframework.http.*;import org.springframework.web.bind.MethodArgumentNotValidException;import org.springframework.web.bind.annotation.*;import java.time.Instant;import java.util.*;
@RestControllerAdvice public class GlobalExceptionHandler {
 @ExceptionHandler(NotFoundException.class) ResponseEntity<?> notFound(NotFoundException e){return error(HttpStatus.NOT_FOUND,e.getMessage(),null);}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e){Map<String,String> fields=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(x->fields.put(x.getField(),x.getDefaultMessage()));return error(HttpStatus.BAD_REQUEST,"Validation failed",fields);}
 @ExceptionHandler({IllegalArgumentException.class, java.time.format.DateTimeParseException.class}) ResponseEntity<?> badRequest(Exception e){return error(HttpStatus.BAD_REQUEST,e.getMessage(),null);}
 @ExceptionHandler(Exception.class) ResponseEntity<?> general(Exception e){return error(HttpStatus.INTERNAL_SERVER_ERROR,"The request could not be completed",null);}
 private ResponseEntity<?> error(HttpStatus s,String m,Object v){return ResponseEntity.status(s).body(Map.of("timestamp",Instant.now(),"status",s.value(),"error",s.getReasonPhrase(),"message",m,"validationErrors",v==null?Map.of():v));}
}
