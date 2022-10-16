import org.litepal.crud.LitePalSupport
import java.io.File

class MusicPojo : LitePalSupport {
    var id = 0
    var musicName: String? = null
    var musicPath: String? = null
    var musicArtist: String? = null
    var musicDuration = 0
    var isLove = false

    constructor() {}
    constructor(file: File) {
        musicName = file.name
        musicPath = file.path
    }
}