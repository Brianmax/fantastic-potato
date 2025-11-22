package com.example.demo.service;

import com.example.demo.dto.UsuarioDTO;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Usuario crearUsuario(UsuarioDTO dto) {
        // BUG SUTIL: La validación de email solo funciona si el email no está en minúsculas
        // Si alguien registra "TEST@example.com" y luego "test@example.com", ambos se permiten
        if (usuarioRepository.existsByEmail(dto.getEmail().toLowerCase(Locale.ROOT))) {
            throw new InvalidOperationException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    public Usuario obtenerUsuario(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if(usuario.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return usuario.get();
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public void desactivarUsuario(Long id) {
        Usuario usuario = obtenerUsuario(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    public boolean estaActivo(Long id) {
        Usuario usuario = obtenerUsuario(id);
        return usuario.getActivo();
    }
}
