package muni_del_valle.ms_integracion.dto;

public class IntegrationEvent {
    private String tipo;
    private String zona;
    private String mensaje;

    public IntegrationEvent() {}

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
