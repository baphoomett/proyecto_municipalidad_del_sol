# Seed script: 30 usuarios normales + 50 reportes en Concepcion, Chile
# Ejecutar desde la raiz del proyecto: .\scripts\seed_data.ps1

$ErrorActionPreference = "SilentlyContinue"
$BASE_URL_AUTH    = "http://localhost:8084/api/auth"
$BASE_URL_REPORTS = "http://localhost:8081/api/reports"

# ─── USUARIOS ────────────────────────────────────────────────────────────────

$usuarios = @(
    @{ fullName = "Camila Rojas Fuentes";     email = "camila.rojas@mail.cl" },
    @{ fullName = "Sebastian Muñoz Vera";     email = "sebastian.munoz@mail.cl" },
    @{ fullName = "Valentina Torres Rios";    email = "valentina.torres@mail.cl" },
    @{ fullName = "Felipe Castillo Soto";     email = "felipe.castillo@mail.cl" },
    @{ fullName = "Isadora Perez Alarcon";    email = "isadora.perez@mail.cl" },
    @{ fullName = "Matias Henriquez Lagos";   email = "matias.henriquez@mail.cl" },
    @{ fullName = "Daniela Vega Espinoza";    email = "daniela.vega@mail.cl" },
    @{ fullName = "Nicolas Sanchez Mora";     email = "nicolas.sanchez@mail.cl" },
    @{ fullName = "Javiera Medina Quiroz";    email = "javiera.medina@mail.cl" },
    @{ fullName = "Diego Ramirez Pinto";      email = "diego.ramirez@mail.cl" },
    @{ fullName = "Constanza Flores Ibañez";  email = "constanza.flores@mail.cl" },
    @{ fullName = "Andres Gutierrez Meza";    email = "andres.gutierrez@mail.cl" },
    @{ fullName = "Catalina Silva Neira";     email = "catalina.silva@mail.cl" },
    @{ fullName = "Rodrigo Contreras Farias"; email = "rodrigo.contreras@mail.cl" },
    @{ fullName = "Fernanda Molina Bravo";    email = "fernanda.molina@mail.cl" },
    @{ fullName = "Pablo Alvarez Cuevas";     email = "pablo.alvarez@mail.cl" },
    @{ fullName = "Antonia Herrera Campos";   email = "antonia.herrera@mail.cl" },
    @{ fullName = "Cristian Diaz Valdes";     email = "cristian.diaz@mail.cl" },
    @{ fullName = "Gabriela Morales Leon";    email = "gabriela.morales@mail.cl" },
    @{ fullName = "Ignacio Vargas Donoso";    email = "ignacio.vargas@mail.cl" },
    @{ fullName = "Francisca Romero Saez";    email = "francisca.romero@mail.cl" },
    @{ fullName = "Eduardo Navarro Cruz";     email = "eduardo.navarro@mail.cl" },
    @{ fullName = "Paz Fuentes Acevedo";      email = "paz.fuentes@mail.cl" },
    @{ fullName = "Tomas Reyes Bustamante";   email = "tomas.reyes@mail.cl" },
    @{ fullName = "Belen Jimenez Araya";      email = "belen.jimenez@mail.cl" },
    @{ fullName = "Maximiliano Parra Sepulveda"; email = "maxi.parra@mail.cl" },
    @{ fullName = "Sofia Zuniga Opazo";       email = "sofia.zuniga@mail.cl" },
    @{ fullName = "Jose Carrasco Poblete";    email = "jose.carrasco@mail.cl" },
    @{ fullName = "Renata Aguilera Trujillo"; email = "renata.aguilera@mail.cl" },
    @{ fullName = "Emilio Campos Henriquez";  email = "emilio.campos@mail.cl" }
)

Write-Host ""
Write-Host ">>> Creando 30 usuarios..."
$creados = 0
foreach ($u in $usuarios) {
    $body = "{`"email`":`"$($u.email)`",`"password`":`"User1234!`",`"fullName`":`"$($u.fullName)`"}"
    try {
        Invoke-RestMethod -Method Post -Uri "$BASE_URL_AUTH/register" -Body $body -ContentType "application/json" -ErrorAction Stop | Out-Null
        $creados++
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        if ($status -eq 400 -or $status -eq 409) {
            Write-Host "  [skip] $($u.email) ya existe"
            $creados++
        } else {
            Write-Host "  [error] $($u.email) - $status"
        }
    }
}
Write-Host ">>> $creados/30 usuarios listos."

# ─── TOKEN ADMIN ─────────────────────────────────────────────────────────────

Write-Host ""
Write-Host ">>> Obteniendo token de admin..."
try {
    $loginResp = Invoke-RestMethod -Method Post -Uri "$BASE_URL_AUTH/login" `
        -Body '{"email":"admin@municipalidad.cl","password":"Admin1234!"}' `
        -ContentType "application/json" -ErrorAction Stop
    $token = $loginResp.token
    Write-Host ">>> Token obtenido."
} catch {
    Write-Host "ERROR: No se pudo obtener el token de admin. Asegurate de haber ejecutado seed_admin.ps1 primero."
    exit 1
}

# ─── REPORTES EN CONCEPCION ──────────────────────────────────────────────────
# Coordenadas reales distribuidas por los barrios de Concepcion, Chile

$reportes = @(
    # ── ACTIVO (25 reportes) ──────────────────────────────────────────────────
    # Centro
    @{ lat=-36.8270; lon=-73.0499; desc="Incendio en edificio de departamentos frente a la Plaza de Armas"; severity="ALTA";  type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8285; lon=-73.0523; desc="Principio de incendio en local comercial sector Mall del Centro"; severity="MEDIA"; type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8261; lon=-73.0511; desc="Incendio en container de basura calle Barros Arana con O'Higgins"; severity="BAJA";  type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8298; lon=-73.0487; desc="Humo visible en edificio de oficinas calle Anibal Pinto"; severity="MEDIA"; type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8274; lon=-73.0534; desc="Incendio en kiosco de diario esquina Rengo con Colo-Colo"; severity="BAJA";  type="INCENDIO"; status="ACTIVO" },
    # Barrio Norte
    @{ lat=-36.8152; lon=-73.0481; desc="Incendio estructural en casa abandonada Av. Los Carrera"; severity="ALTA";  type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8138; lon=-73.0502; desc="Vehiculo en llamas calle Tucapel Norte frente al liceo"; severity="MEDIA"; type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8167; lon=-73.0467; desc="Principio de incendio en bodega sector norte"; severity="BAJA";  type="INCENDIO"; status="ACTIVO" },
    # Pedro de Valdivia
    @{ lat=-36.8301; lon=-73.0748; desc="Incendio en vivienda calle Freire con Pedro de Valdivia"; severity="ALTA";  type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8289; lon=-73.0763; desc="Fuego en pastizales junto a canal Pedro de Valdivia"; severity="MEDIA"; type="INCENDIO"; status="ACTIVO" },
    # Lorenzo Arenas
    @{ lat=-36.8247; lon=-73.0653; desc="Incendio en galpon de materiales calle Lorenzo Arenas"; severity="ALTA";  type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8259; lon=-73.0641; desc="Quema de escombros que se propaga sector Lorenzo Arenas"; severity="MEDIA"; type="INCENDIO"; status="ACTIVO" },
    # Barrio Sur / Estadio
    @{ lat=-36.8441; lon=-73.0579; desc="Incendio en vivienda calle Lamas sector Estadio"; severity="ALTA";  type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8459; lon=-73.0601; desc="Humo intenso en microbasural calle Lientur"; severity="BAJA";  type="INCENDIO"; status="ACTIVO" },
    # Andalien
    @{ lat=-36.8071; lon=-73.0324; desc="Incendio en vivienda calle Los Manios sector Andalien"; severity="ALTA";  type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8058; lon=-73.0347; desc="Fuego en microbasural calle El Roble sector norte Andalien"; severity="BAJA";  type="INCENDIO"; status="ACTIVO" },
    # Costanera Bio Bio
    @{ lat=-36.8199; lon=-73.0441; desc="Incendio en restoran a orillas del rio Bio Bio costanera norte"; severity="MEDIA"; type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8213; lon=-73.0423; desc="Fuego en bote abandonado orilla rio Bio Bio"; severity="BAJA";  type="INCENDIO"; status="ACTIVO" },
    # Lomas Coloradas
    @{ lat=-36.8421; lon=-73.0382; desc="Incendio en cerro con riesgo de propagacion sector Lomas Coloradas"; severity="ALTA";  type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8437; lon=-73.0361; desc="Quema ilegal de pastizales sector oriente Concepcion"; severity="MEDIA"; type="INCENDIO"; status="ACTIVO" },
    # Collao / Terminal
    @{ lat=-36.8304; lon=-73.0463; desc="Incendio en bus interprovincial Terminal Collao"; severity="ALTA";  type="INCENDIO"; status="ACTIVO" },
    # Puente Bicentenario
    @{ lat=-36.8187; lon=-73.0512; desc="Incendio en campamento informal orilla rio Bio Bio bajo puente"; severity="ALTA";  type="INCENDIO"; status="ACTIVO" },
    @{ lat=-36.8174; lon=-73.0534; desc="Quema de colchones en sitio eriazo sector ribera norte"; severity="BAJA";  type="INCENDIO"; status="ACTIVO" },
    # San Pedro de la Paz
    @{ lat=-36.8589; lon=-73.0955; desc="Cortocircuito con amago de incendio edificio San Pedro de la Paz"; severity="BAJA";  type="INCENDIO"; status="ACTIVO" },
    # Chiguayante
    @{ lat=-36.9113; lon=-73.0199; desc="Quema no autorizada de residuos en quebrada Chiguayante"; severity="MEDIA"; type="INCENDIO"; status="ACTIVO" },

    # ── EN_COMBATE (15 reportes) ──────────────────────────────────────────────
    # Pedro de Valdivia
    @{ lat=-36.8315; lon=-73.0721; desc="Cortocircuito con incendio en poste electrico Av. Pedro de Valdivia"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },
    @{ lat=-36.8277; lon=-73.0735; desc="Incendio en local de comida rapida sector Pedro de Valdivia"; severity="MEDIA"; type="INCENDIO"; status="EN_COMBATE" },
    # Lorenzo Arenas
    @{ lat=-36.8238; lon=-73.0667; desc="Principio de incendio en taller mecanico Av. Chacabuco"; severity="MEDIA"; type="INCENDIO"; status="EN_COMBATE" },
    # Barrio Universitario
    @{ lat=-36.8316; lon=-73.0624; desc="Incendio en laboratorio universidad sector Campus UdeC"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },
    @{ lat=-36.8332; lon=-73.0598; desc="Cortocircuito con humo en residencia universitaria Victor Lamas"; severity="MEDIA"; type="INCENDIO"; status="EN_COMBATE" },
    # Hualpen
    @{ lat=-36.7997; lon=-73.0878; desc="Incendio en planta industrial Av. Hualpen sector portuario"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },
    @{ lat=-36.8021; lon=-73.0912; desc="Fuego en contenedor maritimo puerto Hualpen"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },
    @{ lat=-36.8048; lon=-73.0853; desc="Incendio en bodega de distribucion sector Hualpen"; severity="MEDIA"; type="INCENDIO"; status="EN_COMBATE" },
    # San Pedro de la Paz
    @{ lat=-36.8553; lon=-73.1003; desc="Incendio en casa de madera sector Laguna Grande San Pedro"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },
    @{ lat=-36.8538; lon=-73.1021; desc="Incendio en supermercado Av. Jorge Alessandri San Pedro"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },
    # Chiguayante
    @{ lat=-36.9128; lon=-73.0218; desc="Incendio forestal activo cerro Chiguayante avanza hacia viviendas"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },
    @{ lat=-36.9147; lon=-73.0241; desc="Incendio en casa prefabricada sector ribera Bio Bio Chiguayante"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },
    # Talcahuano
    @{ lat=-36.7124; lon=-73.1163; desc="Incendio en nave industrial sector ASMAR Talcahuano"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },
    @{ lat=-36.7101; lon=-73.1178; desc="Incendio en deposito de combustible sector industrial Talcahuano"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },
    # Diagonal PAC
    @{ lat=-36.8361; lon=-73.0512; desc="Incendio en ferreteria Diagonal Pedro Aguirre Cerda"; severity="ALTA";  type="INCENDIO"; status="EN_COMBATE" },

    # ── CONTROLADO (10 reportes) ──────────────────────────────────────────────
    # Barrio Sur / Estadio
    @{ lat=-36.8472; lon=-73.0553; desc="Incendio en casino estudiantil sector Barrio Universitario Sur"; severity="MEDIA"; type="INCENDIO"; status="CONTROLADO" },
    # Hualpen
    @{ lat=-36.7983; lon=-73.0901; desc="Principio de incendio en vivienda calle Los Aromos Hualpen"; severity="BAJA";  type="INCENDIO"; status="CONTROLADO" },
    # San Pedro de la Paz
    @{ lat=-36.8571; lon=-73.0978; desc="Fuego en terreno baldio sector Las Dunas San Pedro de la Paz"; severity="MEDIA"; type="INCENDIO"; status="CONTROLADO" },
    # Talcahuano
    @{ lat=-36.7139; lon=-73.1142; desc="Fuego en embarcacion menor puerto Talcahuano"; severity="MEDIA"; type="INCENDIO"; status="CONTROLADO" },
    # Costanera / Hospital
    @{ lat=-36.8279; lon=-73.0594; desc="Alarma de incendio en Hospital Regional Concepcion"; severity="MEDIA"; type="INCENDIO"; status="CONTROLADO" },
    @{ lat=-36.8292; lon=-73.0571; desc="Humo en salon de eventos calle Castellon"; severity="BAJA";  type="INCENDIO"; status="CONTROLADO" },
    # Diagonal PAC
    @{ lat=-36.8378; lon=-73.0489; desc="Principio de incendio en panaderia sector sur centro"; severity="MEDIA"; type="INCENDIO"; status="CONTROLADO" },
    # Collao
    @{ lat=-36.8318; lon=-73.0441; desc="Principio de incendio en estacion de servicio sector Collao"; severity="ALTA";  type="INCENDIO"; status="CONTROLADO" },
    # O'Higgins
    @{ lat=-36.8245; lon=-73.0487; desc="Incendio en galeria comercial calle OHiggins con Arturo Prat"; severity="MEDIA"; type="INCENDIO"; status="CONTROLADO" },
    @{ lat=-36.8231; lon=-73.0463; desc="Fuego en deposito de neumaticos calle Serrano"; severity="MEDIA"; type="INCENDIO"; status="CONTROLADO" }
)

Write-Host ""
Write-Host ">>> Creando $($reportes.Count) reportes en Concepcion..."
$headers = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }
$emails  = $usuarios | ForEach-Object { $_.email }
$reportCreados = 0
$cntActivo    = 0
$cntEnCombate = 0
$cntControlado = 0

for ($i = 0; $i -lt $reportes.Count; $i++) {
    $r         = $reportes[$i]
    $userEmail = $emails[$i % $emails.Count]

    $body = "{" +
        "`"reporterEmail`":`"$userEmail`"," +
        "`"latitude`":$($r.lat)," +
        "`"longitude`":$($r.lon)," +
        "`"description`":`"$($r.desc)`"," +
        "`"severity`":`"$($r.severity)`"," +
        "`"incidentType`":`"$($r.type)`"," +
        "`"mediaUrls`":[]" +
    "}"

    try {
        $created = Invoke-RestMethod -Method Post -Uri $BASE_URL_REPORTS -Headers $headers -Body $body -ContentType "application/json" -ErrorAction Stop
        $reportCreados++

        # Si el estado deseado no es ACTIVO, actualizarlo via PATCH
        if ($r.status -ne "ACTIVO" -and $null -ne $created.id) {
            $patchBody = "{`"status`":`"$($r.status)`"}"
            try {
                Invoke-RestMethod -Method Patch -Uri "$BASE_URL_REPORTS/$($created.id)/status" -Headers $headers -Body $patchBody -ContentType "application/json" -ErrorAction Stop | Out-Null
            } catch {
                Write-Host "  [warn] reporte $($i+1): no se pudo actualizar estado a $($r.status) (id=$($created.id))"
            }
        }

        switch ($r.status) {
            "ACTIVO"     { $cntActivo++ }
            "EN_COMBATE" { $cntEnCombate++ }
            "CONTROLADO" { $cntControlado++ }
        }
    } catch {
        Write-Host "  [error] reporte $($i+1): $($_.Exception.Response.StatusCode.value__)"
    }
}

Write-Host ">>> $reportCreados/$($reportes.Count) reportes creados."

# ─── RESUMEN ─────────────────────────────────────────────────────────────────

Write-Host ""
Write-Host "=============================================="
Write-Host "  SEED COMPLETADO"
Write-Host "  Usuarios creados : $creados / 30"
Write-Host "  Reportes creados : $reportCreados / $($reportes.Count)"
Write-Host "    - ACTIVO       : $cntActivo"
Write-Host "    - EN_COMBATE   : $cntEnCombate"
Write-Host "    - CONTROLADO   : $cntControlado"
Write-Host "  Password usuarios: User1234!"
Write-Host "  Ciudad           : Concepcion, Chile"
Write-Host "  Barrios cubiertos: Centro, Norte, Pedro de Valdivia,"
Write-Host "                     Lorenzo Arenas, Hualpen, San Pedro"
Write-Host "                     de la Paz, Chiguayante, Talcahuano"
Write-Host "=============================================="
