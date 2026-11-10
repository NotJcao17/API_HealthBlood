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

> ⚠️ Nunca pongas la contraseña en el código ni la subas a GitHub. Solo va en Northflank, en el paso 3.

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

1. Entra a https://northflank.com y pulsa **Sign up**. **Regístrate con GitHub**, así conectas tus repos en un paso.
   - Si te pide elegir plan, elige el **Developer Sandbox** (gratis). Puede que pida una tarjeta para verificar tu identidad, pero no cobra mientras estés en el sandbox.
2. Crea un proyecto:
   - Pulsa **Create new → Project**.
   - Nombre: `healthblood`.
   - Región: la que te ofrezca el plan gratis. Si hay una de EE. UU., elígela, porque queda cerca de TiDB `us-east-1`.
3. Dentro del proyecto pulsa **Create new → Service** y elige **Combined service**, que construye y despliega en un solo paso:
   - **Name:** `api`
   - **Repository:** `API_HealthBlood`. Si no aparece, pulsa *Link GitHub* o *Configure GitHub app* y dale acceso a ese repo.
   - **Branch:** `main`
   - **Build type:** **Dockerfile**, con *Dockerfile location* en `/Dockerfile` y *Build context* en `/`.
4. **Networking / Ports:**
   - Port: `8080`, Protocol: `HTTP`.
   - Activa **Publicly expose this port to the internet**.
5. **Environment variables.** Agrega estas 5 variables con los datos del paso 1. Si hay una opción para marcarlas como *secret*, úsala en la contraseña.

   | Nombre | Valor |
   |---|---|
   | `DB_HOST` | el HOST de TiDB |
   | `DB_PORT` | `4000` |
   | `DB_NAME` | `healthBlood` |
   | `DB_USER` | el USERNAME de TiDB (con el prefijo) |
   | `DB_PASSWORD` | la contraseña de TiDB |

6. **Resources:** deja el plan más pequeño que entre en el sandbox gratis.
7. Pulsa **Create service**. Primero corre el *build*, que tarda 2 o 3 minutos la primera vez, y después el *deployment*. Puedes ver los logs en las pestañas **Builds** y **Containers → Logs**. Cuando Tomcat arranca aparece una línea con `Server startup in [...] milliseconds`.
8. En la página del servicio, en **Ports & DNS**, aparece tu URL pública, parecida a:
   `https://p01--api--abcd1234efgh.code.run`

---

## Paso 4 – Probar

Con la URL que te dio Northflank (cámbiala en estos comandos):

```bash
curl https://TU-URL.code.run/api/glucosa/saludar
```

```bash
curl https://TU-URL.code.run/api/glucosa/getAllUsuarios
```

```bash
curl -X POST https://TU-URL.code.run/api/glucosa/insertMedida -d "idUsuario=1" -d "glucosa=98" -d "contexto=AYUNAS" -d "notas=Primera prueba en linea"
```

```bash
curl https://TU-URL.code.run/api/glucosa/getAllMedidas
```

> La ruta ya **no** lleva `/API_HealthBlood`. En línea, la API vive en la raíz: `https://TU-URL.code.run/api/glucosa/...`.

### Si algo falla
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

Sigue funcionando igual. Si no existen las variables `DB_*`, `ConnectionMysql` usa tu MySQL local (`127.0.0.1`, `root/root`). Como ahora van a trabajar con la base en línea, lo normal es probar directamente contra la URL de Northflank.
