# Validacion del medidor Java contra lizard 1.25.1

- Metodos detectados por lizard: 291
- Metodos detectados por el medidor propio: 419
- Metodos comparables (mismo archivo y linea): 291
- Coincidencia V(G): 291/291
- Coincidencia NLOC: 291/291
- Coincidencia parametros: 291/291
- Metodos que solo detecta el medidor propio: 128 (lizard no reconoce las clases anotadas con argumentos, p. ej. @RequestMapping("/api/...") o @Table(name = ...), ni los metodos dentro de records: omite completos los controladores, las entidades JPA *Data y los DTO record)
- Metodos que solo detecta lizard: 0
