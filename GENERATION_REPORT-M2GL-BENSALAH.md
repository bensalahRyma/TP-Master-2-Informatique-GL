# GENERATION REPORT – Phase 1

## LLM utilisé
ChatGPT GPT-5.1

## PROMPT FINAL
```markdown
Contexte :
Je dois développer un système complet de cache générique en Java 21 pour optimiser l'accès à l'API JeuxDeMots (JDM) du LIRMM.
Les requêtes JDM sont coûteuses, mais les réponses sont stables. Le but est de réduire les appels réseau grâce à un cache performant, thread-safe et configurable.

Objectif :
Générer du code Java 21 COMPLET, COMPILABLE, FONCTIONNEL, sans TODO, sans pseudo-code, avec tous les fichiers nécessaires.
Le code doit suivre strictement les spécifications suivantes.

------------------------------------------------------
🌟 SPÉCIFICATIONS TECHNIQUES OBLIGATOIRES
------------------------------------------------------

1. Interface publique du cache :
public interface Cache<K, V> {
    V get(K key);
    void put(K key, V value);
    void invalidate(K key);
    void clear();
    CacheStats getStats();
}

2. Propriétés du cache :
- Générique : Cache<K, V>
- Thread-safe (accès concurrent correct)
- Configurable : taille max + stratégie d’éviction + TTL optionnel
- Observable : statistiques (hits, misses, taux de succès)
- Performant : get/put en O(1) ou O(log n)

3. Stratégies d’éviction (au moins 2 obligatoires) :
- LRU (Least Recently Used)
- FIFO (First In First Out)
- LFU (Least Frequently Used) — optionnel
- TTL (expiration automatique)
→ Pattern Strategy obligatoire :
    interface EvictionStrategy<K>
    + au moins 2 implémentations complètes

4. Composants obligatoires :
- Cache<K,V> (interface)
- CacheStats (classe immuable ou record Java 21)
- EvictionStrategy<K> (interface)
- Implémentations : LRU + FIFO minimum
- Implémentation concrète du cache :
    GenericConcurrentCache<K,V>
- Structures thread-safe (ConcurrentHashMap, synchronized, locks)
- Aucune suppression dans la stratégie elle-même : l’éviction doit être faite uniquement dans le cache
- Version Java 21 (records bienvenus)

------------------------------------------------------
🌟 INTÉGRATION API JDM
------------------------------------------------------

Créer :
1) JdmClient :
   - Utilise HttpClient Java 11+
   - Méthodes : getTermRaw, getRelationsRaw, getSynonymsRaw, etc.
   - Retour en JSON String ou JsonNode (Jackson)

2) CachedJdmClient :
   - Utilise un Cache<String, String>
   - Avant appel réseau → vérifier le cache
   - En cas de miss → appeler l’API + stocker la réponse
   - Logger les performances (HIT/MISS, temps de réponse)
   - Permettre de modifier dynamiquement la stratégie d’éviction

------------------------------------------------------
🌟 TESTS UNITAIRES JUNIT 5 OBLIGATOIRES
------------------------------------------------------

Inclure des tests COMPLETS :
- Tests basiques : put/get/invalidate/clear
- Tests LRU (vérifier l’ordre exact → B doit être évincé avant A)
- Tests FIFO
- Tests TTL
- Tests de concurrence (ExecutorService)
- Tests sur CacheStats
- Tests CachedJdmClient (avec mock JdmClient)

------------------------------------------------------
🌟 CONTRAINTES
------------------------------------------------------
- Java 21 obligatoire
- Pas de framework externe de cache (pas de Caffeine, pas de Guava)
- Bibliothèques autorisées :
    * Jackson (JSON)
    * SLF4J / Logback
    * HttpClient
- Code ENTIER demandé :
    * chaque fichier Java complet
    * aucun TODO
    * aucune omission

------------------------------------------------------
🌟 SORTIE ATTENDUE
------------------------------------------------------
→ UNIQUEMENT du code Java 21 complet  
→ Chaque fichier clairement séparé avec un commentaire :
// File: src/main/java/.../ClassName.java
→ Inclure également tous les tests JUnit 5  
→ Aucun texte, aucune explication autour du code

Génère maintenant le code Java 21 complet.

```
## Temps de génération
Environ 30 secondes.

## Qualité apparente du code
- Code globalement bien structuré : interfaces, stratégies, JDMClient.
- Architecture cohérente (pattern Strategy respecté).
- Certaines classes très longues.
- Thread-safety incertaine (beaucoup de synchronized).
- Statistiques du cache implémentées.

## Problèmes rencontrés à la compilation
- Import manquant pour HttpClient.
- Classe EvictionStrategy oubliée dans une implémentation.
- Test concurrent non fonctionnel (variables non synchronisées).

## Tests fournis par le LLM
- Tests unitaires de base présents mais incomplets.
- Pas de tests d’éviction.
- Pas de test de concurrence.

## Conclusion
La génération est bonne pour démarrer, mais nécessite un audit approfondi en Phase 2.
