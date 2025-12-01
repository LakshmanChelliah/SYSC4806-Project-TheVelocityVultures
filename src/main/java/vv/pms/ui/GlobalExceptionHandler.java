package vv.pms.ui;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import vv.pms.project.UnauthorizedAccessException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        // This will show a simple error page or message when a Professor tries to hack the URL
        ModelAndView mav = new ModelAndView("error"); // Assumes a default error view exists
        mav.addObject("errorMessage", "Access Denied: " + ex.getMessage());
        mav.addObject("status", 403);
        return mav;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("uploadError", "File is too large! Maximum size is 10MB.");
        
        // Try to redirect back to the referring page
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        
        // Fallback
        return "redirect:/projects"; 
    }
}
