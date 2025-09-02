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

La aplicacion corre sobre una base de datos h2 la cual es una base de datos en memoria pero el servicion esta configurado para guardar la informacion
en un archivo que se ecuentra dentro de la carpeta data, si se elimina la carpeta se pieden los datos guardados hasta ese momento, cuando se reinicie
el servidor va a generarse nuevamente la carpeta con el archivo pero no se van a tener los datos anteriores.

Los distintos endpoint estan documentados por medio de swagger, para acceder al mismo la url es http://localhost:8080/swagger-ui/index.html#/

Para el caso que se desee probar el servicio por medio de postman a continuacion dejo un curl de cada uno de los endpoint

1 Obtener todas las empresas de la base de datos (Paginado)
curl -X 'GET' \
'http://localhost:8080/empresas?page=0&size=10&sort=razonSocial%2CASC' \
-H 'accept: */*'

2 Crear una nueva empresa
curl -X 'POST' \
'http://localhost:8080/empresas' \
-H 'accept: */*' \
-H 'Content-Type: application/json' \
-d '{
"cuit": "23-123233-1",
"razonSocial": "Empresa prueba 4",
"fechaAdhesion": "2025-09-01",
"saldo": 10
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