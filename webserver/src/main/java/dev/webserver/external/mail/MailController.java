package dev.webserver.external.mail;

import dev.webserver.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(path = "${api.endpoint.baseurl}mail")
class MailController extends AbstractEnvironment {

    protected MailController(final Environment environment) {
        super(environment);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/exception")
    public String mailexception(ModelMap map) {
        map.addAttribute("frontend", uiRedirect);
        return "registration-exception";
    }

}
