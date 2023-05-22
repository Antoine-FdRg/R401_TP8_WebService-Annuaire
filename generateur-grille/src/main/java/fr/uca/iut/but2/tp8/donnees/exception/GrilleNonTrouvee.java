package fr.uca.iut.but2.tp8.donnees.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GrilleNonTrouvee extends RuntimeException {
}
