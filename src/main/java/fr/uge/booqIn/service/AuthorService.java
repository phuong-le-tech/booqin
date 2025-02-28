package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthorService {
  private AuthorRepository authorRepository;
  public AuthorService(AuthorRepository authorRepository){
    this.authorRepository = authorRepository;
  }

}
