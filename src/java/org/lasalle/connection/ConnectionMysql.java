/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.connection;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author j_c_o
 */
public class ConnectionMysql {
    
    Connection conn;
    
    // Lee una variable de entorno; si no existe, usa el valor por defecto
    private static String env(String nombre, String porDefecto){
        String valor = System.getenv(nombre);
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }

    public Connection open(){
        // En linea (Northflank) estos valores vienen de variables de entorno con
        // los datos de TiDB. Si no existen, se usa el MySQL local de siempre.
        String host = env("DB_HOST", "127.0.0.1");
        String port = env("DB_PORT", "3306");
        String bdName = env("DB_NAME", "healthBlood");
        String user = env("DB_USER", "root");
        String password = env("DB_PASSWORD", "root");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + bdName;

        // TiDB Cloud solo acepta conexiones cifradas (TLS); el MySQL local no tiene TLS configurado
        boolean esLocal = host.equals("127.0.0.1") || host.equals("localhost");
        String ssl = esLocal
                ? "useSSL=false&allowPublicKeyRetrieval=true"
                : "sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3";
        String parametros = "?" + ssl + "&useUnicode=true&characterEncoding=utf-8";
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url+parametros, user, password);
            return conn;
        } catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public void close(){
        if(conn != null){
            try{
                conn.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
}
