package afanasievald.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WorkController {
    @NotNull
    private final Logger LOGGER = LogManager.getLogger(WorkController.class.getName());

    @Autowired
    public WorkController(                          ) {

    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @ExceptionHandler({ Exception.class})
    public ModelAndView handleException(Exception e)
    {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error");
        modelAndView.addObject("message", "В программе произошла ошибка, обратитесь к администратору");
        LOGGER.error(e.getMessage(), e);
        return modelAndView;
    }
}