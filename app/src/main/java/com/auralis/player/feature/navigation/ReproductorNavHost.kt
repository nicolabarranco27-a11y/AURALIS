package com.auralis.player.feature.navigation

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.auralis.player.BuildConfig
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
import com.auralis.player.feature.favorites.FavoritesScreen
import com.auralis.player.feature.queue.QueueScreen
import com.auralis.player.feature.settings.SettingsScreen
import com.auralis.player.feature.hidden.HiddenSongsScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val SONGS = "songs"
    const val ALBUMS = "albums"
    const val ARTISTS = "artists"
    const val PLAYLISTS = "playlists"
    const val FAVORITES = "favorites"
    const val QUEUE = "queue"
    const val SETTINGS = "settings"
    const val HIDDEN_SONGS = "hidden_songs"
    const val PLAYER = "player"
    const val ALBUM_DETAIL = "album_detail/{albumId}"
    const val ARTIST_DETAIL = "artist_detail/{artistName}"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}"
    const val ADD_TO_PLAYLIST = "add_to_playlist/{playlistId}"
    
    // Los argumentos se codifican: album/artista pueden contener '/', '?', '|',
    // espacios o acentos que romperian el patron de ruta. Navigation decodifica
    // los argumentos de path al leerlos.
    fun albumDetail(albumId: String) = "album_detail/${Uri.encode(albumId)}"
    fun artistDetail(artistName: String) = "artist_detail/${Uri.encode(artistName)}"
    fun playlistDetail(playlistId: String) = "playlist_detail/${Uri.encode(playlistId)}"
    fun addToPlaylist(playlistId: String) = "add_to_playlist/${Uri.encode(playlistId)}"
}

@Composable
fun ReproductorNavHost() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val colors = ReproductorThemeTokens.colors
    val spacing = ReproductorThemeTokens.spacing

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colors.background,
                drawerContentColor = colors.textPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                AuralisDrawerContent(
                    currentRoute = currentRoute,
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
                    onPlayerClick = { navController.navigate(Routes.PLAYER) },
                    onPlaylistClick = { id -> navController.navigate(Routes.playlistDetail(id)) }
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
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onPlayerClick = { navController.navigate(Routes.PLAYER) }
                )
            }
            composable(Routes.QUEUE) {
                QueueScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Routes.ADD_TO_PLAYLIST) {
                AddToPlaylistScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onHiddenSongsClick = { navController.navigate(Routes.HIDDEN_SONGS) },
                )
            }
            composable(Routes.HIDDEN_SONGS) {
                HiddenSongsScreen(
                    onBackClick = { navController.popBackStack() },
                    onPlayerClick = { navController.navigate(Routes.PLAYER) },
                )
            }
            composable(Routes.PLAYER) {
                PlayerScreen(
                    onBackClick = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AuralisDrawerContent(
    currentRoute: String?,
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
        DrawerItem("Inicio", "⌂", isSelected = currentRoute == Routes.HOME, onClick = { onNavigate(Routes.HOME) }, colors, spacing)
        DrawerItem("Canciones", "♪", isSelected = currentRoute == Routes.SONGS, onClick = { onNavigate(Routes.SONGS) }, colors, spacing)
        DrawerItem("Álbumes", "▢", isSelected = currentRoute == Routes.ALBUMS, onClick = { onNavigate(Routes.ALBUMS) }, colors, spacing)
        DrawerItem("Artistas", "👤", isSelected = currentRoute == Routes.ARTISTS, onClick = { onNavigate(Routes.ARTISTS) }, colors, spacing)
        DrawerItem("Playlists", "☰", isSelected = currentRoute == Routes.PLAYLISTS, onClick = { onNavigate(Routes.PLAYLISTS) }, colors, spacing)
        DrawerItem("Favoritos", "", isSelected = currentRoute == Routes.FAVORITES, onClick = { onNavigate(Routes.FAVORITES) }, colors, spacing, iconImageVector = Icons.Rounded.FavoriteBorder)
        DrawerItem("Cola", "≡", isSelected = currentRoute == Routes.QUEUE, onClick = { onNavigate(Routes.QUEUE) }, colors, spacing)
        
        Spacer(modifier = Modifier.height(spacing.l))
        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(spacing.l))

        DrawerItem("Ajustes", "⚙", isSelected = currentRoute == Routes.SETTINGS, onClick = { onNavigate(Routes.SETTINGS) }, colors, spacing)

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
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: AppColors,
    spacing: AppSpacing,
    iconImageVector: ImageVector? = null
) {
    val pillShape = RoundedCornerShape(percent = 50)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(pillShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.secondaryContainer 
                else androidx.compose.ui.graphics.Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = spacing.s, horizontal = spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconImageVector != null) {
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = iconImageVector,
                    contentDescription = null,
                    tint = if (isSelected) colors.accent else colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Text(
                text = icon,
                style = AppType.title.copy(
                    fontSize = 20.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) colors.accent else colors.textSecondary,
                modifier = Modifier.width(32.dp)
            )
        }
        Text(
            text = label,
            style = AppType.body.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) colors.accent else colors.textPrimary
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
            .padding(bottom = spacing.s)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.estalingradocorp_logo),
                contentDescription = "Logo EstalingradoCorp",
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.Black),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(spacing.s))
            Text(
                text = "EstalingradoCorp",
                style = AppType.label.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = AppType.label.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = colors.accent
            )
        }
        
        Spacer(modifier = Modifier.height(spacing.xs))
        
        Text(
            text = "Visita EstalingradoCorp",
            style = AppType.label.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { uriHandler.openUri(corporateUrl) }
        )
        
        Spacer(modifier = Modifier.height(spacing.s))
        
        Text(
            text = "© EstalingradoCorp. Todos los derechos reservados.",
            style = AppType.label.copy(fontSize = 9.sp),
            color = colors.textSecondary.copy(alpha = 0.6f)
        )
    }
}

