# Guía para poner la API en línea (TiDB + Northflank)

```
App Swift  ──HTTPS──▶  Northflank (Docker con Tomcat + tu API)  ──TLS──▶  TiDB Cloud (MySQL)
```

Hay que hacer 4 cosas, en este orden:

1. Crear la base de datos en **TiDB Cloud** y guardar sus datos de conexión.
2. Subir el proyecto a **GitHub**.
3. Crear el servicio en **Northflank**, que toma el código de GitHub, lo compila con el `Dockerfile` y lo publica.
4. Probar con `curl` y usar la URL en la app Swift.

---

## Paso 1 – Base de datos en TiDB Cloud

1. Entra a https://tidbcloud.com y regístrate. Puedes entrar con tu cuenta de GitHub o de Google.
2. Crea un clúster:
   - Si el asistente no aparece solo, pulsa **Create Cluster** o **Create Instance**.
   - Elige el plan **Starter**, que es el gratis.
   - Proveedor **AWS** y una región cercana, por ejemplo `us-east-1 (N. Virginia)`.
   - Ponle un nombre, por ejemplo `healthblood`, y créalo. Tarda menos de un minuto.
3. Crea las tablas:
   - Dentro del clúster abre **SQL Editor**. También puede aparecer como **Chat2Query**.
   - Pega todo el contenido de `database/healthBlood.sql` (está en este proyecto) y ejecútalo. Para ejecutar todo, selecciona el texto y pulsa *Run*.
   - Al final debe aparecer el usuario "Juan Carlos".
4. Obtén los datos de conexión:
   - Pulsa **Connect**, arriba a la derecha.
   - En *Connect With* elige **General** o **Java**.
   - Pulsa **Generate Password**. **Copia la contraseña en ese momento**, porque después no se vuelve a mostrar.
   - Anota estos 4 datos:

     | Dato | Ejemplo | Lo usarás como |
     |---|---|---|
     | HOST | `gateway01.us-east-1.prod.aws.tidbcloud.com` | `DB_HOST` |
     | PORT | `4000` | `DB_PORT` |
     | USERNAME | `2aBcD3eFgH.root` (lleva un prefijo con punto) | `DB_USER` |
     | PASSWORD | la que generaste | `DB_PASSWORD` |

   - Si ves una opción de acceso o lista de IPs permitidas, déjala abierta a todas las IPs (*Allow access from anywhere* o `0.0.0.0/0`). La IP de Northflank puede cambiar. La conexión sigue protegida con contraseña y TLS.

> ⚠️ Nunca pongas la contraseña en el código ni la subas a GitHub. Va solo en dos lugares: en Northflank (paso 3) y en `src/java/db.properties` para NetBeans (ver "¿Y NetBeans?" al final).

---

## Paso 2 – Subir el proyecto a GitHub

El proyecto ya es un repositorio git. Abre una terminal en la carpeta del proyecto (`API_HealthBlood`) y ejecuta:

```bash
git add .
git commit -m "API HealthBlood lista para desplegar"
```

Luego crea el repositorio en GitHub y súbelo. Ya tienes instalado `gh` (la herramienta de GitHub) y has iniciado sesión con él:

```bash
gh repo create API_HealthBlood --private --source . --push
```

Puede ser `--private`, porque Northflank puede leer repositorios privados. Para comprobarlo, abre https://github.com/NotJcao17/API_HealthBlood y revisa que estén `Dockerfile`, `src/`, `web/` y `lib/`.

> Cada vez que cambies el código, haz `git add .`, `git commit -m "..."` y `git push`. Northflank detecta el push y vuelve a desplegar solo.

---

## Paso 3 – Publicar la API en Northflank

> 💳 Northflank pide a **todos** los usuarios un método de pago antes de crear servicios, aunque uses el plan gratis (**Developer Sandbox**: 2 servicios, 2 jobs y 1 base de datos). Según su documentación, es para verificar identidad y evitar abusos. El Sandbox no se puede escalar, así que no subes de plan sin darte cuenta.

### 3.1 Cuenta y proyecto
1. Regístrate en https://northflank.com con GitHub y elige el plan **Developer Sandbox**.
2. Crea un proyecto llamado `healthblood`. Northflank asigna la región; por ejemplo, *US - Central*.

### 3.2 Conectar GitHub a Northflank
Registrarte con GitHub **no** le da acceso a tus repositorios; eso se hace aparte.
1. En el formulario del servicio, en la sección **Source**, aparece *"Connect a version control account to get started"*. Pulsa **GitHub**. También puedes hacerlo desde https://app.northflank.com/s/account/integrations/vcs con **Link GitHub**.
2. Elige tu team de Northflank, por ejemplo `notjcaos-team`.
3. Se abre GitHub con la instalación de la app **Northflank**:
   - Cuenta: `NotJcao17`.
   - **Only select repositories** → `API_HealthBlood`.
   - Pulsa **Install** o **Save**.
   - Si la app ya estaba instalada y el botón Save está desactivado, cambia la opción de repositorios (por ejemplo, a *All* y de vuelta a *Only select*) para que se active.
4. GitHub te regresa a Northflank, y la sección **Source** ya muestra la lista de repositorios.

### 3.3 Crear el servicio (Create new → Service)
Llena el formulario de arriba hacia abajo:

1. **Tipo:** **Combined – Build and deploy a Git repo**.
2. **Service name:** `api`. ⚠️ El nombre **no se puede cambiar** después.
3. **Source:**
   - **Repository:** `NotJcao17/API_HealthBlood`.
   - **Branch:** `main`.
4. **Build options:**
   - **Build type:** **Dockerfile**.
   - **Build context:** `/`
   - **Dockerfile location:** `/Dockerfile`
   - Normalmente estos dos valores ya vienen así por defecto, porque el Dockerfile está en la raíz del repo.
5. **Environment variables.** Agrega estas 5 como *runtime variables* (no como *build arguments*), con los datos del paso 1:

   | Key | Value |
   |---|---|
   | `DB_HOST` | el HOST de TiDB |
   | `DB_PORT` | `4000` |
   | `DB_NAME` | `healthBlood` |
   | `DB_USER` | el USERNAME de TiDB (con el prefijo) |
   | `DB_PASSWORD` | la contraseña de TiDB |

   Puedes agregarlas una por una (key/value) o pegarlas todas en el modo de texto o `.env`.
6. **Networking / Ports:** Northflank detecta el puerto `8080` gracias al `EXPOSE` del Dockerfile. Revisa que quede así:
   - **Port:** `8080`
   - **Protocol:** **HTTP**
   - **Publicly expose / Public:** activado
   - Si no aparece, pulsa **Add port** y llénalo con esos valores.
7. **Resources:** deja el plan que viene por defecto, el más pequeño del Sandbox.
8. **Advanced** (health checks, CMD, volúmenes): no hace falta tocar nada.
9. Pulsa **Create service**.

### 3.4 Esperar el despliegue y obtener la URL
1. Primero se ejecuta el **build**. La primera vez tarda 2 o 3 minutos. En la pestaña **Builds** puedes abrir el build para ver su log.
2. Después arranca el contenedor. En los logs del servicio (pestaña **Logs** u **Observe**) debe aparecer `Server startup in [...] milliseconds`.
3. La URL pública aparece en el encabezado del servicio como **Public DNS**, con un formato parecido a:
   `https://p01--api--abcd1234efgh.code.run`
4. Si después necesitas cambiar una variable, ve a la pestaña **Environment** del servicio, edítala y guarda. El servicio se reinicia solo.

---

## Paso 4 – Probar

1. **Copia la URL con el ícono de copiar** que está junto al dominio, arriba a la derecha de la página del servicio. No la escribas a mano: tiene letras que se confunden, como `l`, `1` e `i`. También puedes abrirla con el ícono ↗.
2. Pruébala en PowerShell con **`curl.exe`**, no con `curl`. En PowerShell, `curl` es otro comando (`Invoke-WebRequest`) que no entiende `-X` ni `-d`. Cambia `TU-URL` por la tuya:

```bash
curl.exe https://TU-URL.code.run/api/glucosa/saludar
```

```bash
curl.exe https://TU-URL.code.run/api/glucosa/getAllUsuarios
```

```bash
curl.exe -X POST https://TU-URL.code.run/api/glucosa/insertMedida -d "idUsuario=1" -d "glucosa=98" -d "contexto=AYUNAS" -d "notas=Primera prueba en linea"
```

```bash
curl.exe https://TU-URL.code.run/api/glucosa/getAllMedidas
```

Para probar solo los GET también puedes abrir la URL en el navegador.

> La ruta ya **no** lleva `/API_HealthBlood`. En línea, la API vive en la raíz: `https://TU-URL.code.run/api/glucosa/...`.

### Si algo falla
- **`No se puede resolver el nombre remoto` / `Could not resolve host`**: ese dominio no existe en el DNS.
  1. Revisa que la URL sea exactamente la que copiaste con el ícono.
  2. En **Networking**, el puerto `8080` debe ser **HTTP** y **Public**. El dominio solo se crea si el puerto es público.
  3. Si acabas de crear el servicio o el puerto, espera unos minutos a que el DNS se publique.
- **`{"result": "Empty table"}` o `Error al insertar...`**: la API funciona, pero no llega a la base. Revisa en Northflank → *Containers → Logs* el error de Java y verifica las 5 variables. Lo más común es un `DB_USER` sin el prefijo o una contraseña mal copiada.
- **El build falla**: abre *Builds* y revisa el log. Comprueba que `lib/` se haya subido a GitHub.
- **La URL no responde**: revisa que el puerto `8080` esté expuesto como público y que el contenedor esté en estado *Running*.

---

## Usarla desde Swift

- URL base: `https://TU-URL.code.run/api/glucosa/`
- Al ser HTTPS, iOS la acepta sin configurar nada extra (App Transport Security).
- Los `GET` devuelven JSON. Los `POST` reciben **form-urlencoded**:

```swift
var req = URLRequest(url: URL(string: "https://TU-URL.code.run/api/glucosa/insertMedida")!)
req.httpMethod = "POST"
req.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
req.httpBody = "idUsuario=1&glucosa=98&contexto=AYUNAS&notas=Desde%20iOS".data(using: .utf8)
let (data, _) = try await URLSession.shared.data(for: req)
```

---

## ¿Y NetBeans?

`ConnectionMysql` busca cada dato de conexión en este orden:

| Orden | Dónde busca | Cuándo se usa |
|---|---|---|
| 1 | Variables de entorno `DB_*` | **Northflank**: las que pusiste en el panel |
| 2 | Archivo `src/java/db.properties` | **NetBeans**: NetBeans lo copia junto a las clases al compilar |
| 3 | Valores por defecto (`127.0.0.1`, `3306`, `root/root`) | Si no hay ninguno de los anteriores, se conecta a tu MySQL local |

`db.properties` está en `.gitignore`, así que **la contraseña nunca se sube a GitHub** y el repo puede ser público. Al correr el proyecto en NetBeans usas **la misma base en línea** que la app Swift.

- **En otra computadora** (por ejemplo, la de un compañero que clone el repo): debe copiar `src/java/db.properties.example` como `src/java/db.properties` y llenar los datos de TiDB.
- **Para volver al MySQL local**: renombra o borra `db.properties`, haz *Clean and Build* y el código usará `127.0.0.1` con `root/root`. Con un host local, la conexión se hace sin TLS automáticamente.
