/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.rest;
import com.google.gson.Gson;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.connection.MedidaController;
import org.lasalle.connection.UsuarioController;
import org.lasalle.model.Medida;
import org.lasalle.model.Usuario;

@Path("glucosa")
public class RestGlucosa {
    
    @Path("saludar")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludar(){
        String saludo = """
                        {"Saludo":"Hola desde java"}
                        """;
        return Response.ok(saludo).build();
    }
    
    @Path("saludarCumpleanios")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludarCumpleanios(
            @QueryParam("nombre") String nombre
    ){
        String out = """
                     {"response":"Feliz cumple %s"}
                     """;
        out = String.format(out, nombre);
        return Response.ok(out).build();
    }
    
    @Path("getAllUsuarios")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsuarios(){
        UsuarioController uc = new UsuarioController();
        List<Usuario> lista = new ArrayList<>();
        Gson gson = new Gson();
        String out = "";
        try {
            lista = uc.getUsuarios();
            out = gson.toJson(lista);
        } catch (Exception e) {
            e.printStackTrace();
            out = """
                   {"result": "Empty table"}
                   """;
        }
        return Response.ok(out).build();
    }

    @Path("getAllMedidas")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMedidas(){
        MedidaController mc = new MedidaController();
        List<Medida> lista = new ArrayList<>();
        Gson gson = new Gson();
        String out = "";
        try {
            lista = mc.getMedidas();
            out = gson.toJson(lista);
        } catch (Exception e) {
            e.printStackTrace();
            out = """
                   {"result": "Empty table"}
                   """;
        }
        return Response.ok(out).build();
    }

    @Path("insertUsuario")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertUsuario(
            @FormParam("nombre") String nombre,
            @FormParam("primerApellido") String primerApellido,
            @FormParam("segundoApellido") String segundoApellido,
            @FormParam("fechaNacimiento") String fechaNacimiento,
            @FormParam("tipoDiabetes") String tipoDiabetes
    ){
        UsuarioController uc = new UsuarioController();
        String out = "";
        try {
            uc.insertUsuario(nombre, primerApellido, segundoApellido, fechaNacimiento, tipoDiabetes);
            out = """
                  {"result": "Usuario insertado"}
                  """;
        } catch (Exception e) {
            e.printStackTrace();
            out = """
                  {"result": "Error al insertar usuario"}
                  """;
        }
        return Response.ok(out).build();
    }

    @Path("insertMedida")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertMedida(
            @FormParam("idUsuario") int idUsuario,
            @FormParam("glucosa") int glucosa,
            @FormParam("contexto") String contexto,
            @FormParam("notas") String notas
    ){
        MedidaController mc = new MedidaController();
        String out = "";
        try {
            mc.insertMedida(idUsuario, glucosa, contexto, notas);
            out = """
                  {"result": "Medida insertada"}
                  """;
        } catch (Exception e) {
            e.printStackTrace();
            out = """
                  {"result": "Error al insertar medida"}
                  """;
        }
        return Response.ok(out).build();
    }

}
