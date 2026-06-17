# Guía rápida: ejecutar las pruebas y generar reportes

## 1. Ejecutar las pruebas de un microservicio

Desde la carpeta de cada módulo:

```bash
cd ms_usuarios       # o ms_reportes / ms_monitoreo / ms_alertas / ms_integracion
mvn test
# Windows sin Maven instalado: usar el wrapper
mvnw.cmd test
```

Esto compila y corre todas las clases bajo `src/test/java`. El resultado se imprime en consola, y cada clase deja un reporte individual en:

```
target/surefire-reports/<paquete>.<Clase>.txt
target/surefire-reports/<paquete>.<Clase>.xml
```

> Nota: en todos los módulos vas a ver fallar `*ApplicationTests.contextLoads` si no tenés PostgreSQL/RabbitMQ levantado (vía `docker-compose up -d`). No es un test unitario, es un smoke test de arranque del contexto Spring; las pruebas unitarias y de `@WebMvcTest` no dependen de infraestructura externa.

## 2. Ejecutar las pruebas de todos los microservicios juntos

El POM raíz agrupa `ms_usuarios`, `ms_reportes`, `ms_monitoreo`, `ms_alertas` y `ms_integracion` (no incluye `api_gateway` ni `bff`, que tienen su propio POM independiente):

```bash
mvn test
```

Para correr `api_gateway` o `bff` hay que entrar a su carpeta:

```bash
cd api_gateway && mvn test
cd bff && mvn test
```

## 3. Filtrar y correr una sola clase o un solo test

```bash
mvn test -Dtest=UserServiceTest
mvn test -Dtest=ReportServiceTest#createReport_deberiaGuardarReporteYEventoInicial
```

## 4. Generar un reporte de cobertura de código (JaCoCo)

El proyecto no tiene JaCoCo configurado por defecto. Para generarlo en un módulo puntual:

1. Agregar el plugin al `<build><plugins>` del `pom.xml` del módulo:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

2. Correr las pruebas normalmente:

```bash
mvn test
```

3. Abrir el reporte HTML generado:

```
target/site/jacoco/index.html
```

Ahí se ve el % de cobertura de líneas/ramas por paquete y por clase, con drill-down hasta el código fuente resaltado en verde/rojo.

> Si querés cobertura agregada de los 5 microservicios en un solo reporte, hay que repetir el paso 1 en cada `pom.xml` y revisar cada `target/site/jacoco/index.html` por separado (JaCoCo no agrega módulos Maven independientes automáticamente sin un módulo `report-aggregate` extra).

## 5. Generar un informe legible (PDF/Word) a partir de los resultados

Los resultados ya documentados de la última corrida (tablas de casos, esperado vs. obtenido) están en la sección "Pruebas unitarias" del `README.md` de cada microservicio, y compilados como informe ejecutivo con gráficos en `reports/informe_pruebas_unitarias.pdf`.

Si se vuelve a correr la suite y se quiere actualizar ese informe:
1. Ejecutar `mvn test` en cada módulo y anotar pasados/fallidos de la salida de consola o de `target/surefire-reports/*.txt`.
2. Actualizar las tablas correspondientes en cada `README.md`.
3. Regenerar el PDF (el informe se armó con un HTML propio impreso a PDF vía Edge/Chrome headless: `msedge --headless --print-to-pdf=informe.pdf informe.html`).
