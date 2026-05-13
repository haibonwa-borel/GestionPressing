Collection Postman pour le projet GestionPressing

Importation:

- Ouvrir Postman -> Import -> choisir `postman/GestionPressing.postman_collection.json`.
- Importer l'environnement `postman/GestionPressing.postman_environment.json`.

Utilisation rapide:

- Mettre `baseUrl` dans l'environnement (ex: `http://localhost:8080`).
- Pour les endpoints protégés, remplir `authToken` puis ajouter un header `Authorization: Bearer {{authToken}}`.
- Pour les requêtes multipart, sélectionner un fichier local pour le champ `photo` ou `image`.

Exécution via Newman (optionnel):

```bash
# installer newman si nécessaire
npm install -g newman

# exécuter la collection avec l'environnement
newman run postman/GestionPressing.postman_collection.json -e postman/GestionPressing.postman_environment.json
```
