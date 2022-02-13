package net.numalab.hiraganacraft

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.io.Reader
import java.lang.Exception

class HiraganaConverter(private val jaFileReader: Reader) {
    private val gson = Gson()
    private var json = loadFile()

    /**
     * Load Ja_Jp from File
     */
    private fun loadFile(): Map<String, String>? {
        return try {
            val rootElement = gson.fromJson(jaFileReader, JsonElement::class.java)
            val jsonObject = rootElement.asJsonObject
            jsonObject.entrySet().toMutableList().associate { it.key to it.value.asString }
        } catch (e: Exception) {
            // Failed
            null
        }
    }

    // Translation Key -> Material?
    operator fun get(translationKey: String): Material? {
        return Material.values().find { it.translationKey == translationKey }
    }

    // ItemStack -> Material -> String
    operator fun get(material: Material): String? {
        val value = json?.get(material.translationKey)
        return if (value != null) {
            mapString(value)
        } else {
            null
        }
    }

    operator fun get(itemStack: ItemStack) = get(itemStack.type)

    fun getAllEntries() = json?.entries?.toList() ?: listOf()

    private fun validateChar(char: Char): Boolean {
        return map.keys.contains(char)
    }

    fun toCustomModelDataInt(char: Char): Int? {
        val mappedChar = mapChar(char)
        return map[mappedChar]
    }

    fun fromCustomModelDataInt(int: Int): Char? {
        return map.entries.find { it.value == int }?.key
    }

    fun fromHiraganaCard(itemStack: ItemStack): Char? {
        if (itemStack.type == Material.PAPER && itemStack.itemMeta.hasCustomModelData()) {
            return fromCustomModelDataInt(itemStack.itemMeta.customModelData)
        }
        return null
    }
    /**
     * Convert String to Valid Chars
     */
    fun toChars(str: String): List<Char> {
        return str.toCharArray().toMutableList().map { mapChar(it) }.filter { validateChar(it) }
    }

    fun mapString(str: String): String {
        return toChars(str).joinToString("")
    }

    /**
     * 文字を変換
     * 小文字　→　大文字
     * 濁点、半濁点　→　取る
     */
    fun mapChar(char: Char): Char {
        when (char) {
            'ぁ' -> return 'あ'
            'ぇ' -> return 'え'
            'ぃ' -> return 'い'
            'ぉ' -> return 'お'
            'っ' -> return 'つ'
            'ぅ' -> return 'う'
            'ゃ' -> return 'や'
            'ょ' -> return 'よ'
            'ゅ' -> return 'ゆ'
            'ゎ' -> return 'わ'
            'が' -> return 'か'
            'ぎ' -> return 'き'
            'ぐ' -> return 'ぐ'
            'げ' -> return 'け'
            'ご' -> return 'こ'
            'ざ' -> return 'さ'
            'じ' -> return 'し'
            'ず' -> return 'す'
            'ぜ' -> return 'せ'
            'ぞ' -> return 'そ'
            'だ' -> return 'た'
            'ぢ' -> return 'ち'
            'づ' -> return 'つ'
            'で' -> return 'て'
            'ど' -> return 'と'
            'ば' -> return 'は'
            'び' -> return 'ひ'
            'ぶ' -> return 'ふ'
            'べ' -> return 'へ'
            'ぼ' -> return 'ほ'
            'ぱ' -> return 'は'
            'ぴ' -> return 'ひ'
            'ぷ' -> return 'ふ'
            'ぺ' -> return 'へ'
            'ぽ' -> return 'ほ'
            else -> return char
        }
    }

    private val map = listOf(
        'あ',
        'ち',
        'え',
        'ふ',
        'は',
        'へ',
        'ひ',
        'ほ',
        'い',
        'か',
        'け',
        'き',
        'こ',
        'く',
        'ま',
        'め',
        'み',
        'も',
        'む',
        'ん',
        'な',
        'ね',
        'に',
        'の',
        'ぬ',
        'お',
        'ら',
        'れ',
        'り',
        'ろ',
        'る',
        'さ',
        'せ',
        'し',
        'そ',
        'す',
        'た',
        'て',
        'と',
        'つ',
        'う',
        'わ',
        'を',
        'や',
        'よ',
        'ゆ',
        'ー',
        'ぁ',
        'ぇ',
        'ぃ',
        'ぉ',
        'っ',
        'ぅ',
        'ゃ',
        'ょ',
        'ゅ'
    ).mapIndexed { index, c -> c to (index + 1) }.toMap()
}