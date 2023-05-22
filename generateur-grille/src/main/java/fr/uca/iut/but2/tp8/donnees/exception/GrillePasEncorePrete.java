package fr.uca.iut.but2.tp8.donnees.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PARTIAL_CONTENT)
public class GrillePasEncorePrete extends RuntimeException {
}
