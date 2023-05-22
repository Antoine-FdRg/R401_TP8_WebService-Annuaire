package fr.uca.iut.but2.tp8;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class GrilleApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrilleApplication.class, args);
	}

	@Bean public CommandLineRunner lireLesParamètres() {
		return args -> {
			System.out.println(Arrays.toString(args));
			if (args.length == 2) {
				int longueur = Integer.parseInt(args[0]);
				int hauteur = Integer.parseInt(args[1]);
				System.out.println(longueur+ " x "+hauteur);
			}
		} ;
	}
}
