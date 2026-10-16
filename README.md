# Apple Music Offline

Player de música offline com interface inspirada no Apple Music.

## Características

- Interface baseada no Apple Music (cores #FA233B, #FF2D55)
- Reprodução offline de música local
- Biblioteca organizada (Songs, Albums, Artists, Playlists)
- Busca local
- Letras (planejado)
- Equalizador (planejado)

## Tecnologias

| Componente | Tecnologia |
|------------|------------|
| Linguagem | Kotlin |
| UI | Jetpack Compose |
| Player | Media3 (ExoPlayer) |
| Banco de dados | Room |
| Injeção de dependência | Hilt |
| Arquitetura | MVVM |

## Estrutura

```
app/src/main/java/com/applemusic/offline/
├── AppleMusicApplication.kt
├── MainActivity.kt
├── data/
│   ├── MediaScanner.kt
│   ├── model/
│   │   ├── Song.kt
│   │   ├── Album.kt
│   │   ├── Artist.kt
│   │   └── Playlist.kt
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   ├── SongDao.kt
│   │   └── PlaylistDao.kt
│   └── repository/
│       └── SongRepository.kt
├── di/
│   └── AppModule.kt
├── service/
│   └── MediaPlaybackService.kt
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── player/
│   │   └── PlayerScreen.kt
│   ├── library/
│   │   └── LibraryScreen.kt
│   └── search/
│       └── SearchScreen.kt
└── viewmodel/
    ├── PlayerViewModel.kt
    ├── LibraryViewModel.kt
    └── SearchViewModel.kt
```

## Como compilar

### Opção 1: Android Studio
1. Abra o projeto no Android Studio
2. Sync Gradle
3. Build > Generate Signed Bundle/APK

### Opção 2: Gradle CLI
```bash
./gradlew assembleDebug
```

### Opção 3: Online (GitHub Actions, Codemagic)
Configure CI/CD para build automático.

## Fontes

As fontes SF Pro precisam ser extraídas do APK original do Apple Music ou baixadas do site da Apple.

## Licença

Projeto para uso pessoal e educacional.

## Compilando com GitHub Actions

### Passo a passo:

1. **Crie um repositório no GitHub**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/seu-usuario/apple-music-offline.git
   git push -u origin main
   ```

2. **Ative o GitHub Actions**
   - Vá em "Actions" no seu repositório
   - Ative o workflow "Build APK"

3. **Faça um push para compilar**
   ```bash
   git push
   ```

4. **Baixe o APK**
   - Vá em "Actions" > último workflow
   - Baixe o artifact "app-debug" ou "app-release"

### Vantagens do GitHub Actions:

| Vantagem | Descrição |
|----------|-----------|
| Grátis | 2000 minutos/mês para repositórios públicos |
| Automático | Compila a cada push |
| Sem instalação local | Não precisa do Android Studio |
| Histórico | Mantém versões anteriores |

### Alternativas:

| Serviço | Plano gratuito |
|---------|---------------|
| GitHub Actions | 2000 min/mês |
| Codemagic | 500 min/mês |
| Bitrise | 200 min/mês |
| AppCenter | Grátis (limitado) |
