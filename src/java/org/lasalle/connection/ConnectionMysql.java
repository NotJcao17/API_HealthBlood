/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.connection;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 *
 * @author j_c_o
 */
public class ConnectionMysql {

    Connection conn;

    // Datos del archivo db.properties (src/java/db.properties). Git lo ignora,
    // asi que la contrasena nunca se sube a GitHub.
    private static final Properties DB_PROPS = new Properties();
    static {
        try (InputStream in = ConnectionMysql.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                DB_PROPS.load(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Busca un dato en este orden:
    // 1. Variable de entorno (asi funciona en Northflank)
    // 2. Archivo db.properties (asi funciona en NetBeans)
    // 3. Valor por defecto (MySQL local)
    private static String config(String nombre, String porDefecto){
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            valor = DB_PROPS.getProperty(nombre);
        }
        return (valor == null || valor.isBlank()) ? porDefecto : valor.trim();
    }

    public Connection open(){
        String host = config("DB_HOST", "127.0.0.1");
        String port = config("DB_PORT", "3306");
        String bdName = config("DB_NAME", "healthBlood");
        String user = config("DB_USER", "root");
        String password = config("DB_PASSWORD", "root");
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
