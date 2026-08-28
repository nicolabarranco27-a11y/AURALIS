package com.auralis.player.feature.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.auralis.player.R
import com.auralis.player.core.ui.theme.AppColors
import com.auralis.player.core.ui.theme.AppSpacing
import com.auralis.player.core.ui.theme.AppType
import com.auralis.player.core.ui.theme.ReproductorThemeTokens
import com.auralis.player.feature.home.HomeScreen
import com.auralis.player.feature.player.PlayerScreen
import com.auralis.player.feature.songs.SongsScreen
import com.auralis.player.feature.albums.AlbumsScreen
import com.auralis.player.feature.albums.AlbumDetailScreen
import com.auralis.player.feature.artists.ArtistsScreen
import com.auralis.player.feature.artists.ArtistDetailScreen
import com.auralis.player.feature.playlists.PlaylistsScreen
import com.auralis.player.feature.playlists.PlaylistDetailScreen
import com.auralis.player.feature.playlists.AddToPlaylistScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val SONGS = "songs"
    const val ALBUMS = "albums"
    const val ARTISTS = "artists"
    const val PLAYLISTS = "playlists"
    const val SETTINGS = "settings"
    const val PLAYER = "player"
    const val ALBUM_DETAIL = "album_detail/{albumId}"
    const val ARTIST_DETAIL = "artist_detail/{artistName}"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}"
    const val ADD_TO_PLAYLIST = "add_to_playlist/{playlistId}"
    
    fun albumDetail(albumId: String) = "album_detail/$albumId"
    fun artistDetail(artistName: String) = "artist_detail/$artistName"
    fun playlistDetail(playlistId: String) = "playlist_detail/$playlistId"
    fun addToPlaylist(playlistId: String) = "add_to_playlist/$playlistId"
}

@Composable
fun ReproductorNavHost() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colors.background,
                drawerContentColor = colors.textPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                AuralisDrawerContent(
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        if (route != Routes.HOME) {
                            navController.navigate(route)
                        } else {
                            navController.popBackStack(Routes.HOME, inclusive = false)
                        }
                    },
                    colors = colors,
                    spacing = spacing
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onPlayerClick = { navController.navigate(Routes.PLAYER) }
                )
            }
            composable(Routes.SONGS) {
                SongsScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onPlayerClick = { navController.navigate(Routes.PLAYER) }
                )
            }
            composable(Routes.ALBUMS) {
                AlbumsScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onAlbumClick = { albumId -> navController.navigate(Routes.albumDetail(albumId)) }
                )
            }
            composable(Routes.ALBUM_DETAIL) {
                AlbumDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onPlayerClick = { navController.navigate(Routes.PLAYER) }
                )
            }
            composable(Routes.ARTISTS) {
                ArtistsScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onArtistClick = { artistName -> navController.navigate(Routes.artistDetail(artistName)) }
                )
            }
            composable(Routes.ARTIST_DETAIL) {
                ArtistDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onPlayerClick = { navController.navigate(Routes.PLAYER) },
                    onAlbumClick = { albumId -> navController.navigate(Routes.albumDetail(albumId)) }
                )
            }
            composable(Routes.PLAYLISTS) {
                PlaylistsScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onPlaylistClick = { id -> navController.navigate(Routes.playlistDetail(id)) }
                )
            }
            composable(Routes.PLAYLIST_DETAIL) {
                PlaylistDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddSongsClick = { id -> navController.navigate(Routes.addToPlaylist(id)) },
                    onPlayerClick = { navController.navigate(Routes.PLAYER) }
                )
            }
            composable(Routes.ADD_TO_PLAYLIST) {
                AddToPlaylistScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) { PlaceholderScreen("Ajustes", onBack = { navController.popBackStack() }) }
            composable(Routes.PLAYER) {
                PlayerScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun AuralisDrawerContent(
    onNavigate: (String) -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l)
    ) {
        // Drawer Header
        Text(
            text = "AURALIS",
            style = AppType.display.copy(fontSize = 28.sp),
            color = colors.textPrimary,
            fontWeight = FontWeight.Light,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(vertical = spacing.xl)
        )

        Spacer(modifier = Modifier.height(spacing.m))

        // Navigation Items
        DrawerItem("Inicio", "⌂", onClick = { onNavigate(Routes.HOME) }, colors, spacing)
        DrawerItem("Canciones", "♪", onClick = { onNavigate(Routes.SONGS) }, colors, spacing)
        DrawerItem("Álbumes", "▢", onClick = { onNavigate(Routes.ALBUMS) }, colors, spacing)
        DrawerItem("Artistas", "👤", onClick = { onNavigate(Routes.ARTISTS) }, colors, spacing)
        DrawerItem("Playlists", "☰", onClick = { onNavigate(Routes.PLAYLISTS) }, colors, spacing)
        
        Spacer(modifier = Modifier.height(spacing.l))
        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(spacing.l))

        DrawerItem("Ajustes", "⚙", onClick = { onNavigate(Routes.SETTINGS) }, colors, spacing)

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(spacing.xl))

        // Corporate Section
        CorporateSection(colors, spacing)
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: String,
    onClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ReproductorThemeTokens.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = spacing.m, horizontal = spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = AppType.title.copy(fontSize = 20.sp),
            color = colors.textSecondary,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = label,
            style = AppType.body.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary
        )
    }
}

@Composable
private fun CorporateSection(
    colors: AppColors,
    spacing: AppSpacing
) {
    val uriHandler = LocalUriHandler.current
    val corporateUrl = "https://estalingradocorp.github.io/EstalingradoCorp/"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = spacing.m)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.estalingradocorp_logo),
                contentDescription = "Logo EstalingradoCorp",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.Black),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(spacing.m))
            Text(
                text = "EstalingradoCorp",
                style = AppType.label.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(spacing.s))
        
        Text(
            text = "Visita nuestro sitio oficial",
            style = AppType.label.copy(fontSize = 11.sp),
            color = colors.accent,
            modifier = Modifier.clickable { uriHandler.openUri(corporateUrl) }
        )
        
        Spacer(modifier = Modifier.height(spacing.m))
        
        Text(
            text = "© EstalingradoCorp. Todos los derechos reservados.",
            style = AppType.label.copy(fontSize = 10.sp),
            color = colors.textSecondary.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(spacing.l)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Text("←", color = colors.textPrimary, style = AppType.title)
            }
            Spacer(modifier = Modifier.width(spacing.m))
            Text(
                text = title.uppercase(),
                style = AppType.title,
                color = colors.textPrimary,
                letterSpacing = 2.sp
            )
        }
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Próximamente",
                style = AppType.body,
                color = colors.textSecondary
            )
        }
    }
}
