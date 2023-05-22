package fr.uca.iut.but2.tp8;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

public abstract class BaseWebController {

    @PostMapping("/serviceeteint")
    public void serviceEteint() {
        serviceQuiDisparait();
    }

    public abstract void serviceQuiDisparait();
}

