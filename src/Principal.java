import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Principal {
    private static String apiKey="https://v6.exchangerate-api.com/v6/a81fae513bd072ac2bc06a8d";
    private static List<String> historialConversiones=new ArrayList<>();
    private static HttpClient client=HttpClient.newHttpClient();
    public static void main(String[] args) throws Exception {
        Scanner teclado=new Scanner(System.in);
        System.out.println("Bienvenido Al Conversor de Monedas!");

        while (true){
            System.out.println("Por Favor Elija Una Opción.");
            System.out.println("1. Convertir Moneda");
            System.out.println("2. Muestra Códigos de Monedas Disponibles");
            System.out.println("3. Muestra el Historial de Conversiones");
            System.out.println("4. Muestra una Operación de Ejemplo");
            System.out.println("5. Terminar Aplicación");

            String eleccion=teclado.nextLine();

            switch (eleccion){
                case "1":
                    convertirMonedas(teclado);
                    break;
                case "2":
                    muestraCodigosDisponibles();
                    break;
                case "3":
                    muestraHistorial();
                    break;
                case "4":
                    muestraEjemploAleatorio();
                    break;
                case "5":
                    System.out.println("Transaccion finalizada, hasta luego!");
                    return;
                default:
                    System.out.println("Opción inválida. Por favor intente de nuevo.");
            }
        }
    }

    private static JsonObject tipoDeCambio(String primeraMoneda) throws Exception{
        HttpRequest request=HttpRequest.newBuilder()
                .uri(URI.create(apiKey+"/latest/"+primeraMoneda))
                .build();
        HttpResponse<String> response=client.send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()==200){
            return JsonParser.parseString(response.body()).getAsJsonObject();
        }else{
            System.out.println("Falló el request");
            return null;
        }
    }

    private static JsonObject getCodigosDisponibles() throws Exception{
        HttpRequest request=HttpRequest.newBuilder()
                .uri(URI.create(apiKey+"/codes"))
                .GET().build();

        HttpResponse<String> response=client.send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()==200){
            JsonObject data=JsonParser.parseString(response.body()).getAsJsonObject();
            return data;
        }else{
            throw new Exception("Falló en conseguir los códigos");
        }
    }

    private static void muestraCodigosDisponibles()throws Exception{
        JsonObject codigosDisponibles=getCodigosDisponibles();
        JsonArray jsonArray=codigosDisponibles.getAsJsonArray("supported_codes");

        System.out.println("Códigos de Divisas Disponibles:");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonArray divisa=jsonArray.get(i).getAsJsonArray();
            System.out.println(divisa.get(0).getAsString()+" - "+divisa.get(1).getAsString());
        }
    }

    private static void muestraHistorial(){
        if(!historialConversiones.isEmpty()){
            System.out.println("Historial de operaciones:");
            for (String historial:historialConversiones){
                System.out.println(historial);
            }
        }else{
            System.out.println("No hay operaciones disponibles.");
        }
    }

    private static void convertirMonedas(Scanner scanner) throws Exception{
        System.out.println("Escriba el código de la divisa a cambiar (Ej., MXN): ");
        String divisaBase=scanner.nextLine().toUpperCase();

        JsonObject tipoCambio=tipoDeCambio(divisaBase);
        if(tipoCambio!=null){
            System.out.println("Escriba el código de la divisa a la que quiere cambiar (Ej., USD): ");
            String divisaCambio=scanner.nextLine().toUpperCase();
            System.out.println("Escriba la cantidad a cambiar: ");
            double cantidad=Double.parseDouble(scanner.nextLine());

            if(tipoCambio.get("conversion_rates").getAsJsonObject().has(divisaCambio)){
                double tasa=tipoCambio.get("conversion_rates").getAsJsonObject().get(divisaCambio).getAsDouble();
                double cantidadConvertida=tasa*cantidad;
                System.out.printf("%.2f %s es equivalente a %.2f %s%n", cantidad,divisaBase,cantidadConvertida,divisaCambio);

                DateTimeFormatter formateado=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String marcaTiempo= LocalDateTime.now().format(formateado);
                historialConversiones.add(marcaTiempo+" - "+cantidad+" "+divisaBase+" -> "+
                        cantidadConvertida+" "+divisaCambio);
            }else{
                System.out.println("La divisa a cambiar no fue encontrada");
            }
        }else{
            System.out.println("Error al encontrar la tasa de cambio.");
        }
    }

    private static void muestraEjemploAleatorio() throws Exception{
        JsonObject monedasDisponibles=getCodigosDisponibles();
        JsonArray codigosDisponibles=monedasDisponibles.getAsJsonArray("supported_codes");
        Random random=new Random();
        int monedaIndice1=random.nextInt(codigosDisponibles.size());
        int monedaIndice2=random.nextInt(codigosDisponibles.size());
        while (monedaIndice1==monedaIndice2){
            monedaIndice2=random.nextInt(codigosDisponibles.size());
        }
        String[] moneda1=codigosDisponibles.get(monedaIndice1).getAsJsonArray().toString().split(",");
        String[] moneda2=codigosDisponibles.get(monedaIndice2).getAsJsonArray().toString().split(",");

        String codigo1=moneda1[0].replace("[","").replace("\"","");
        String nombre1=moneda1[1].replace("]","").replace("\"","");
        String codigo2=moneda2[0].replace("[","").replace("\"","");
        String nombre2=moneda2[1].replace("]","").replace("\"","");

        int cantidadAlAzar= random.nextInt(100)+1;

        JsonObject tasaConversion=tipoDeCambio(codigo1);
        double tasaCambio=tasaConversion.getAsJsonObject("conversion_rates").get(codigo2).getAsDouble();
        double cantidadConvertida=cantidadAlAzar*tasaCambio;

        System.out.println("Operación de muestra: ");
        System.out.printf("Convertimos %d %s (%s) a %s (%s): %d %s = %.2f %s%n",
                cantidadAlAzar,codigo1,nombre1,codigo2,nombre2,cantidadAlAzar,codigo1,cantidadConvertida,codigo2);
    }

}
