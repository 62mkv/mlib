using System.Security.Cryptography;

namespace RedPrairie.MOCA.Client.Crypto
{
    /// <summary> 
    /// An implementation of IEncryptionStrategy that utilizes the Blowfish
    /// protocol. It uses a fixed key and PKCS7 padding with an ECB mode.
    /// Note the base code was ported from blowfish.c in misSrvLib of MOCA.
    /// 
    /// blowfish.c:  C implementation of the Blowfish algorithm.
    ///
    /// Base Code concepts Copyright (C) 1997 by Paul Kocher
    ///
    /// This library is free software; you can redistribute it and/or
    /// modify it under the terms of the GNU Lesser General Public
    /// License as published by the Free Software Foundation; either
    /// version 2.1 of the License, or (at your option) any later version.
    /// This library is distributed in the hope that it will be useful,
    /// but WITHOUT ANY WARRANTY; without even the implied warranty of
    /// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    /// Lesser General Public License for more details.
    /// You should have received a copy of the GNU Lesser General Public
    /// License along with this library; if not, write to the Free Software
    /// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
    ///
    ///
    ///
    ///
    /// COMMENTS ON USING THIS CODE:
    ///
    /// Normal usage is as follows:
    ///   [1] Allocate a BLOWFISH_CTX.  (It may be too big for the stack.)
    ///   [2] Call Initialize with a pointer to your BLOWFISH_CTX, a pointer to
    ///       the key, and the number of bytes in the key.
    ///   [3] To encrypt a 64-bit block, call Blowfish_Encrypt with a pointer to
    ///       BLOWFISH_CTX, a pointer to the 32-bit left half of the plaintext
    ///       and a pointer to the 32-bit right half.  The plaintext will be
    ///       overwritten with the ciphertext.
    ///   [4] Decryption is the same as encryption except that the plaintext and
    ///       ciphertext are reversed.
    ///
    ///-- Paul Kocher
    /// </summary>
    public class BlowfishEncryptionStrategy : SymmetricAlgorithm
    {
        #region Private Constants
        private const int BLOCK_SIZE = 8;
        private const int TRANSFORM_SIZE = BLOCK_SIZE*2;
        #endregion

        #region Private Fields

        private RNGCryptoServiceProvider cryptoProvider;
        
        #endregion

        #region SymmerticAlgorithm Overrides

        ///<summary>
        ///When overridden in a derived class, creates a symmetric encryptor object with the specified <see cref="P:System.Security.Cryptography.SymmetricAlgorithm.Key"></see> property and initialization vector (<see cref="P:System.Security.Cryptography.SymmetricAlgorithm.IV"></see>).
        ///</summary>
        ///
        ///<returns>
        ///A symmetric encryptor object.
        ///</returns>
        ///
        ///<param name="rgbIV">The initialization vector to use for the symmetric algorithm. </param>
        ///<param name="rgbKey">The secret key to use for the symmetric algorithm. </param>
        public override ICryptoTransform CreateEncryptor(byte[] rgbKey, byte[] rgbIV)
        {
            return new BlowfishCryptoTransform(true);
        }

        ///<summary>
        ///When overridden in a derived class, creates a symmetric decryptor object with the specified <see cref="P:System.Security.Cryptography.SymmetricAlgorithm.Key"></see> property and initialization vector (<see cref="P:System.Security.Cryptography.SymmetricAlgorithm.IV"></see>).
        ///</summary>
        ///
        ///<returns>
        ///A symmetric decryptor object.
        ///</returns>
        ///
        ///<param name="rgbIV">The initialization vector to use for the symmetric algorithm. </param>
        ///<param name="rgbKey">The secret key to use for the symmetric algorithm. </param>
        public override ICryptoTransform CreateDecryptor(byte[] rgbKey, byte[] rgbIV)
        {
            return new BlowfishCryptoTransform(false);
        }

        ///<summary>
        ///When overridden in a derived class, generates a random key (<see cref="P:System.Security.Cryptography.SymmetricAlgorithm.Key"></see>) to use for the algorithm.
        ///</summary>
        ///
        public override void GenerateKey()
        {
            KeyValue = BlowfishCryptoTransform.BLOWFISH_KEY_BYTES;
        }

        ///<summary>
        ///When overridden in a derived class, generates a random initialization vector (<see cref="P:System.Security.Cryptography.SymmetricAlgorithm.IV"></see>) to use for the algorithm.
        ///</summary>
        ///
        public override void GenerateIV()
        {
            if (cryptoProvider == null)
                cryptoProvider = new RNGCryptoServiceProvider();

            IVValue = new byte[TRANSFORM_SIZE];

            cryptoProvider.GetBytes(IVValue);
        }

        #endregion
        
        #region Override ToString()

        /// <summary>
        /// Returns a <see cref="T:System.String"></see> that represents the current <see cref="T:System.Object"></see>.
        /// </summary>
        /// <returns>
        /// A <see cref="T:System.String"></see> that represents the current <see cref="T:System.Object"></see>.
        /// </returns>
        public override string ToString()
        {
            return "blowfish";
        }

        #endregion
    }
}