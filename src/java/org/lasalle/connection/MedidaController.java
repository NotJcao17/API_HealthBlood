/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.connection;

import java.util.ArrayList;
import java.util.List;
import org.lasalle.connection.ConnectionMysql;
import org.lasalle.model.Medida;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class MedidaController{

    public List<Medida> getMedidas() throws SQLException{
        List<Medida> lista = new ArrayList<>();
        String sql = "SELECT * FROM lectura_glucosa;";
        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();

        while(rs.next()){
            Medida medida = new Medida();
            medida.setIdMedida(rs.getInt("id_lectura"));
            medida.setIdUsuario(rs.getInt("id_usuario"));
            medida.setGlucosa(rs.getInt("valor_mg_dl"));
            medida.setContexto(rs.getString("tipo_lectura"));
            medida.setNotas(rs.getString("notas"));
            medida.setFechaHora(rs.getString("fecha_registro"));
            lista.add(medida);
        }
        rs.close();
        connMysql.close();
        conn.close();
        return lista;

    }

    public boolean insertMedida(int idUsuario, int glucosa, String contexto, String notas) throws SQLException{
        String sql = "INSERT INTO lectura_glucosa (id_usuario, valor_mg_dl, tipo_lectura, notas, fecha_registro) VALUES (?, ?, ?, ?, CURDATE());";
        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setInt(1, idUsuario);
        pstm.setInt(2, glucosa);
        pstm.setString(3, contexto);
        pstm.setString(4, notas);
        int filas = pstm.executeUpdate();
        pstm.close();
        connMysql.close();
        conn.close();
        return filas > 0;
    }

}
