//
// Created by werner on 26.12.15.
//

#ifndef LIBZRTPCPP_EMOJIBASE32_H
#define LIBZRTPCPP_EMOJIBASE32_H
/*
 *
 * Copyright (c) 2002 of the original Base32 Bryce "Zooko" Wilcox-O'Hearn
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software to deal in this software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of this software, and to permit persons to whom this software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of this software.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THIS SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THIS SOFTWARE.
 *
 * Converted to C++ and adding the emojis by:
 * @author Werner Dittmann <Werner.Dittmann@t-online.de>
 */

/**
 * @file EmojiBase32.h
 * @brief C++ Implementation of the Base32 encoding and decoding using Emojis
 *
 * ZRTP uses the base 32 encoding to generate the Short Authentication String (SAS).
 * @ingroup GNU_ZRTP
 * @{
 */

#include <VialerPJSIP/string>
#include <VialerPJSIP/memory>

using namespace std;

extern int divceil(int a, int b);

class EmojiBase32 {
public:
    /**
     * A Constructor that encodes binary data.
     *
     * The constructor converts the first specified number of bits of
     * the binary data into a base32 presentation. Use
     * <code>getEncoded</code> to get the encoded data.
     *
     * @param data
     *    A pointer to the first bits (byte) of binary data
     * @param noOfBits
     *    How many bits to use for encoding. Should be a
     *    multiple of 5.
     */
    EmojiBase32(const unsigned char* data, size_t noOfBits);

    ~EmojiBase32() {}

    /**
     * Get the encoded base32 string.
     *
     * The method returns a string that contains the base32 encoded
     * data if the appropriate constructor was used. Otherwise we
     * return an empty string.
     *
     * @return
     *     The string containing the base32 encoded data.
     */
    const u32string& getEncoded() { return encoded; }

    /**
     * Compute the number of base32 encoded characters given the
     * number of bits.
     *
     * @param lengthInBits
     *      The length of the data in bits
     * @return
     *      The length of the base-32 encoding of the data in characters
     */
    static size_t const b2alen(const size_t lengthInBits) {
        return divceil(lengthInBits, 5);
    }

    /**
     * @brief Convert an UTF-32 encoded string to an UTF-8 encoded string.
     *
     */
    static shared_ptr<string> u32StringToUtf8(const u32string& in);

private:

    void b2a_l(const unsigned char* cs, size_t len, const size_t noOfBits);

    /**
     * The string containing the base32 encoded u32string data.
     */
    u32string encoded;
};


/**
 * @}
 */
#endif //LIBZRTPCPP_EMOJIBASE32_H
