/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.connection;

import java.util.ArrayList;
import java.util.List;
import org.lasalle.connection.ConnectionMysql;
import org.lasalle.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UsuarioController{
    
    public List<Usuario> getUsuarios() throws SQLException{
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM users;";
        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        
        while(rs.next()){
            Usuario user = new Usuario();
            user.setIdUsuario(rs.getInt("id_usuario"));         
            user.setNombre(rs.getString("nombre"));  
            user.setPrimerApellido(rs.getString("primer_apellido"));
            user.setSegundoApellido(rs.getString("segundo_apellido"));
            lista.add(user);
        }
        rs.close();
        connMysql.close();
        conn.close();
        return lista;

    }

    public boolean insertUsuario(String nombre, String primerApellido, String segundoApellido,
            String fechaNacimiento, String tipoDiabetes) throws SQLException{
        String sql = "INSERT INTO users (nombre, primer_apellido, segundo_apellido, fecha_nacimiento, tipo_diabetes, fecha_registro) VALUES (?, ?, ?, ?, ?, CURDATE());";
        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, nombre);
        pstm.setString(2, primerApellido);
        pstm.setString(3, segundoApellido);
        pstm.setString(4, fechaNacimiento);
        pstm.setString(5, tipoDiabetes);
        int filas = pstm.executeUpdate();
        pstm.close();
        connMysql.close();
        conn.close();
        return filas > 0;
    }

}
