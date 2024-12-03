# Visualisation_des_dependances_logicielles_JAVA
Un plugin Maven pour visualiser les dépendances logicielles d'un projet Maven sous forme de graphes interactifs dans Cytoscape. Il automatise l'extraction, le traitement et la visualisation des dépendances grâce à un fichier .dot converti en CSV coloré.
## Informations générales

## Fonctionnalités

    - Clonage automatique des projets Git publics.
    - Génération de fichiers .dot pour représenter les dépendances d’un projet Maven.
    - Conversion des fichiers .dot en CSV adaptés à Cytoscape.
    - Coloration des nœuds par groupes de dépendances.
    - Intégration avec Cytoscape pour visualiser les graphes interactifs.

## Installation
### Prérequis

    - Maven installé et configuré sur votre système.
    - Cytoscape installé (minimum version 3.10.3).
    - Accès à un dépôt Git contenant un projet Maven.

### Ajout du plugin au pom.xml

Ajoutez le plugin comme dépendance dans votre projet Maven :

<build>
  <plugins>
    <plugin>
      <groupId>com.dependence.plugins</groupId>
      <artifactId>cytoscape-dot-plugin</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </plugin>
  </plugins>
</build>

## Utilisation

    Exécutez la commande suivante dans votre terminal ou IDE :

mvn com.dependence.plugins:cytoscape-dot-plugin:generate-graph -DrepoUrl=<URL_du_projet_Git>

    Exemple :

        mvn com.dependence.plugins:cytoscape-dot-plugin:generate-graph -DrepoUrl=https://github.com/jhy/jsoup

    Résultats :
        Un fichier .dot contenant les dépendances du projet.
        Un fichier CSV coloré généré automatiquement.
        Cytoscape s'ouvre avec le graphe généré.

## Exemples de visualisations

    Projet jsoup :

    Projet GraphHopper :

## Perspectives futures

    Intégration des numéros de versions dans les graphes.
    Support d’autres outils de gestion de dépendances comme Gradle.
    Génération de graphes interactifs avec des technologies web.

## Contribuer

Les contributions sont les bienvenues ! Suivez les étapes suivantes pour contribuer :

    Clonez le projet :

git clone https://github.com/MarielLF/Visualisation_des_dependances_logicielles_JAVA.git

## Créez une branche pour vos modifications :

    git checkout -b feature/ma-nouvelle-fonctionnalite

    Proposez une pull request.

## Liens utiles

    Cytoscape
    Maven
    Exemple de plugin Maven
