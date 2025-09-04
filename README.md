A continuacion se deja una descripcion del servicio.

El servicio sooft-challenge-ms se ajusta a los requrimientos del challenge y ademas agrega funcionalidades para una mayor facilidad de uso.

El servicio consta de tres modulos: 
    -   DOMAIN : 
        •Propósito Principal: Contiene toda la lógica, las reglas y los modelos de negocio. 
        •Contenido Clave:
            •Modelos (/model): Clases de dominio Empresa y Transferencia, que encapsulan tanto los datos como el comportamiento del negocio (ej. empresa.debitar()).
            •Puertos (/port): Son las interfaces que definen los contratos de comunicación.
                •Puertos de Entrada (/in): Interfaces UseCase que exponen las capacidades del negocio (ej. AdherirEmpresaUseCase, RealizarTransferenciaUseCase). 
                 Definen las acciones que el mundo exterior puede solicitar.
                •Puertos de Salida (/out): Interfaces RepositoryPort que definen las necesidades de persistencia o comunicación externa (ej. EmpresaRepositoryPort).
                •Excepciones (/exception): Excepciones personalizadas que representan errores de negocio específicos (ej. SaldoInsuficienteException, 
                 CuitDuplicadoException).
    -   APPLICATION :
        •Propósito Principal: Este módulo actúa como un puente entre el dominio y la infraestructura. Su responsabilidad es orquestar los flujos de trabajo y los casos de uso definidos en el dominio.
        •Contenido Clave:
            •Servicios (/service): Clases que implementan las interfaces UseCase del dominio. Por ejemplo, EmpresaService implementa AdherirEmpresaUseCase.
        •Características y Reglas:
            •Depende del domain, pero no del infrastructure: Conoce los modelos y puertos del dominio, pero no tiene idea de si la base de datos es SQL, si la API es REST o si se usan colas de mensajes.
    -   INFRASTRUCTURE : 
        • Propósito Principal: Proporciona las implementaciones concretas para los puertos definidos en el dominio.
        •Contenido Clave:
            •Adaptadores de Entrada (/adapter/in):
            •Controladores Web (/web): Clases @EmpresaController, @TransferenciaController que exponen la API REST.
            •Manejadores de Excepciones (/handler): El GlobalExceptionHandler que traduce las excepciones del dominio a respuestas HTTP.
            •Persistencia (/persistence): La implementación concreta de los RepositoryPort.
            •Entidades JPA (/entity): Clases anotadas con @Empresa, @Transferencia, que se mapean a las tablas de la base de datos.
            •Repositorios Spring Data (/repository): Interfaces que extienden JpaRepository.

Los distintos endpoint estan documentados por medio de swagger, para acceder al mismo la url es http://localhost:8080/swagger-ui/index.html#/

Para el caso que se desee probar el servicio por medio de postman a continuacion dejo un curl de cada uno de los endpoint

1 Obtener todas las empresas de la base de datos (Paginado)
curl -X 'GET' \
'http://localhost:8080/empresas?page=0&size=10&sort=razonSocial%2CASC' \
-H 'accept: */*'

2 Crear una nueva empresa (Para obtener distintos tipos de Idempotency-Key se puede utilizar https://www.uuidtools.com/v4)
curl -X 'POST' \
'http://localhost:8080/empresas' \
-H 'accept: */*' \
-H 'Idempotency-Key: 2c79cf54-a153-45b1-9c42-be5bf6f05fc4' \
-H 'Content-Type: application/json' \
-d '{
"cuit": "23123999122",
"razonSocial": "Empresa test 3",
"fechaAdhesion": "2025-04-04",
"saldo": 1000
}'

3 Obtener todas las empresas adheridas en el ultimo mes
curl -X 'GET' \
'http://localhost:8080/empresas/reportes/adheridas-ultimo-mes?page=0&size=10' \
-H 'accept: */*'

4 Obtener todas las empresas con transferencias en el ultimo mes (Paginado)
curl -X 'GET' \
'http://localhost:8080/empresas/reportes/transferencias-ultimo-mes?page=0&size=10' \
-H 'accept: */*'

5 Realizar una transferencia
curl -X 'POST' \
'http://localhost:8080/transferencias' \
-H 'accept: */*' \
-H 'Content-Type: application/json' \
-d '{
"idEmpresaCredito": "028C5F",
"cuentaCredito": "5C7002AB49DF4C4",
"cuentaDebito": "47D4E768DEF3427",
"importe": 16.34
}'

6 Obtener una empresa po su id
curl -X 'GET' \
'http://localhost:8080/empresas/028C5F' \
-H 'accept: */*'

El proceso para recorrer el servicio es el siguiente:
    - Crear una nueva empresa (Para el caso de querer realizar una transaccion es obligacion tener mas de una empresa)
    - Consultar una empresa por su id
    - Consultar las empresas creadas en el mes actual
    - Relizar una transferencia
    - Consultar las empresas con transferencias en el mes actual

El servicio ofrece el actuator correspondiente (http://localhost:8080/actuator)
    - /actuator/health: Muestra el estado de salud general de la aplicación y sus componentes (base de datos, disco, etc.).
    - /actuator/info: Expone información general y personalizada de la aplicación .
    - /actuator/metrics: Lista todas las métricas disponibles para monitorizar (uso de memoria, CPU, peticiones HTTP, etc.).
    - /actuator/prometheus: Expone las métricas en un formato compatible con el sistema de monitorización Prometheus.
    - /actuator/loggers: Permite ver y modificar los niveles de log de la aplicación en tiempo real.
    - /actuator/env: Muestra todas las propiedades de configuración del entorno de Spring.
    - /actuator/prometheus: Expone las métricas en un formato compatible con el sistema de monitorización Prometheus.
    - /actuator/configprops: Lista todos los beans de configuración (@ConfigurationProperties) y sus valores.
    - /actuator/loggers: Permite ver y modificar los niveles de log de la aplicación en tiempo real.
    - /actuator/beans: Muestra una lista completa de todos los beans cargados en el contexto de la aplicación.
    - /actuator/mappings: Detalla todos los endpoints (@RequestMapping) de la aplicación.
    - /actuator/threaddump: Genera un volcado de hilos (thread dump) para diagnosticar problemas de concurrencia.
    - /actuator/env: Muestra todas las propiedades de configuración del entorno de Spring.
    - /actuator/heapdump: Genera un volcado de memoria (heap dump) para analizar el uso de la memoria.
    - /actuator/configprops: Lista todos los beans de configuración (@ConfigurationProperties) y sus valores.
    - /actuator/mappings: Detalla todos los endpoints (@RequestMapping) de la aplicación.
    - /actuator/beans: Muestra una lista completa de todos los beans cargados en el contexto de la aplicación.
    - /actuator/loggers: Permite ver y modificar los niveles de log de la aplicación en tiempo real.
    - /actuator/metrics: Lista todas las métricas disponibles para monitorizar (uso de memoria, CPU, peticiones HTTP, etc.).
    - /actuator/prometheus: Expone las métricas en un formato compatible con el sistema de monitorización Prometheus.
    - /actuator/mappings: Detalla todos los endpoints (@RequestMapping) de la aplicación.
    - /actuator/health: Muestra el estado de salud general de la aplicación y sus componentes (base de datos, disco, etc.).
    - /actuator/threaddump: Genera un volcado de hilos (thread dump) para diagnosticar problemas de concurrencia.
    - /actuator/threaddump: Genera un volcado de hilos (thread dump) para diagnosticar problemas de concurrencia.
    - /actuator/threaddump: Genera un volcado de hilos (thread dump) para diagnosticar problemas de concurrencia.
    - /actuator/heapdump: Genera un volcado de memoria (heap dump) para analizar el uso de la memoria.

Base de datos utilizada: (http://localhost:8080/h2-console/login.jsp?jsessionid=3f46b6e8ec9e2a5e9091362a0c6a1286. Usuario: sa, Password: password)
La aplicacion corre sobre una base de datos h2, el servicion esta configurado para guardar la informacion
en un archivo que se ecuentra dentro de la carpeta data, si se elimina la carpeta se pieden los datos guardados hasta ese momento, 
cuando se reinicie el servidor va a generarse nuevamente la carpeta con el archivo pero no se van a tener los datos anteriores.