package br.com.crud.dynamodb.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.crud.dynamodb.dto.ScoreDTO;
import br.com.crud.dynamodb.entity.PlayerHistoryEntity;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@RestController
@RequestMapping("/v1/players")
public class PlayerController {
    private final DynamoDbTemplate dynamoDbTemplate;

    public PlayerController(DynamoDbTemplate dynamoDbTemplate) {
        this.dynamoDbTemplate = dynamoDbTemplate;
    }

    @GetMapping("/{username}/games/{gameId}")
    public ResponseEntity<PlayerHistoryEntity> findById(@PathVariable("username") String username, 
                                                        @PathVariable("gameId") String gameId) {
        
        // Realiza uma busca na entidade a partir de uma chave
        var entity = dynamoDbTemplate.load(Key.builder()
                .partitionValue(username)
                .sortValue(gameId)                                       
                .build(), PlayerHistoryEntity.class);

        // Utilizando operador ternário, se não houver registro, retorna um erro 404, senão retorna 200
        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @GetMapping("/{username}/games")
    public ResponseEntity<List<PlayerHistoryEntity>> list(@PathVariable("username") String username) {

        // Cria uma chave com base na chave primária para a query de consulta
        var key = Key.builder().partitionValue(username).build();
        
        // Cria uma condição a partir da chave criada acima
        var condition = QueryConditional.keyEqualTo(key);

        // Constrói a query de consulta
        var query_consultation = QueryEnhancedRequest.builder()
                .queryConditional(condition)
                .build();

        // Invoca a query de consulta a partir do Dyanamo Template
        var history = dynamoDbTemplate.query(query_consultation, PlayerHistoryEntity.class);

        // Retorna sucesso invocando a varável history fazendo a conversão da mesma de objeto iterável para lista
        return ResponseEntity.ok(history.items().stream().toList());
    }

    @PostMapping("/{username}/games")
    public ResponseEntity<Void> save(@PathVariable("username") String username, 
                                    @RequestBody ScoreDTO scoreDTO) {
        
        var entity = PlayerHistoryEntity.fromScore(username, scoreDTO);
        dynamoDbTemplate.save(entity);
        
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/games/{gameId}")
    public ResponseEntity<Void> update(@PathVariable("username") String username,
                                        @PathVariable("gameId") String gameId,
                                        @RequestBody ScoreDTO scoreDTO) {
        
        var entity = dynamoDbTemplate.load(Key.builder()
                .partitionValue(username)
                .sortValue(gameId)
                .build(), PlayerHistoryEntity.class);
        
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        // Altera a pontuação do jogador
        entity.setScore(scoreDTO.score());

        dynamoDbTemplate.save(entity);

        return ResponseEntity.noContent().build();
    }

    // @DeleteMapping("/{username}/games/{gameId}")
    // public ResponseEntity<Void> delete(@PathVariable("username") String username, 
    //                                     @PathVariable("gameId") String gameId) {
        
    
    // }
}
