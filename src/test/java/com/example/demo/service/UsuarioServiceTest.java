package com.example.demo.service;

import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



class UsuarioServiceTest {

    // no se conecta con la base de datos real, solo simulamos su compartamiento
    // Mock --> crea un objeto de UsuarioRepository (new UsuarioRespository)
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioDTO usuarioDTO;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuarioService = new UsuarioService(usuarioRepository);
        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setNombre("George");
        usuarioDTO.setEmail("george@gmail.com");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("George");
        usuario.setEmail("george@gmail.com");

    }

    @Test
    void crearUsuario() {
        // arrange: Preparacion del escenario

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // act: Ejecucion del metodo que estamos probando

        Usuario resultado = usuarioService.crearUsuario(usuarioDTO);

        // assert: verificacion del resultado

        assertNotNull(resultado, "El usuario creado debe de ser no nulo");
        assertEquals("George", resultado.getNombre());
        assertEquals("george@gmail.com", resultado.getEmail());
        assertTrue(resultado.getActivo());
    }

    @Test
    void emailValidation() {
        // arrange
        when(usuarioRepository.existsByEmail("george@gmail.com")).thenReturn(true);
        when(usuarioRepository.existsByEmail("GEORGE@gmail.com")).thenReturn(false);

        UsuarioDTO nuevoUsuario = new UsuarioDTO();
        nuevoUsuario.setNombre("George Maxi");
        nuevoUsuario.setEmail("GEORGE@gmail.com");

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        assertDoesNotThrow(()->{
            Usuario resultado = usuarioService.crearUsuario(nuevoUsuario);

            assertNotNull(resultado);
        });
    }

    @Test
    void obtenerUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.obtenerUsuario(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerUsuarioException() {
        // arrange
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // act y assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> usuarioService.obtenerUsuario(99L),
                "Se debe de lanzar una exception de tipo ResourceNotFoundException"
        );
    }

    @Test
    void obtenerTodos() {
    }

    @Test
    void desactivarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.desactivarUsuario(1L);

        assertFalse(usuario.getActivo());

    }

    @Test
    void estaActivo() {
    }
}