import React, { useState,useEffect } from 'react';
import axios from 'axios';

function App() {

  const [uploadStatus, setUploadStatus] = useState('');
  const [desiredLength, setDesiredLength] = useState(10);
    const [password, setPassword] = useState('');

    const [passwordShown, setPasswordShown] = useState(false);
    const [error, setError] = useState('');
    const [generatedPassword, setGeneratedPassword] = useState('');
    const [fingerprint, setFingerprint] = useState(null);
    const [secretImage, setSecretImage] = useState(null);
// Modified to handle cases where no event is passed
const handleFingerprintUpload = async (event) => {
    event?.preventDefault();
    if (!fingerprint) return; // Ensure there's a file to upload
    const formData = new FormData();
    formData.append('image', fingerprint);
    await axios.post('/fingerPrint', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
    setUploadStatus('Images uploaded successfully');
};

// Similarly update for secret image
const handleSecretImageUpload = async (event) => {
    event?.preventDefault();
    if (!secretImage) return; // Ensure there's a file to upload
    const formData = new FormData();
    formData.append('image', secretImage);
    await axios.post('/image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
    setUploadStatus('Images uploaded successfully');
};

// Updated useEffect
useEffect(() => {
  if (fingerprint) {
    handleFingerprintUpload();
  }
}, [fingerprint]);

useEffect(() => {
  if (secretImage) {
    handleSecretImageUpload();
  }
}, [secretImage]);


    document.body.style = `
//      background: url('./background') no-repeat center center fixed;
      background-size: cover;
      margin: 0;
      height: 100vh;
      overflow: hidden;
    `;

    const handleLengthChange = (event) => {
      const newLength = parseInt(event.target.value, 10);
      if (password.length > newLength) {
        setPassword(password.substring(0, newLength));
      }
      setDesiredLength(newLength);
    };

    const handlePasswordChange = (event) => {
      const inputPassword = event.target.value;
      if (inputPassword.length <= desiredLength) {
        setPassword(inputPassword);
      }
      if (inputPassword.length < desiredLength / 2) {
        setError(`Password must be at least half of the desired length: ${Math.ceil(desiredLength / 2)} characters.`);
      } else {
        setError('');
      }
    };


    const togglePasswordVisibility = () => {
      setPasswordShown(!passwordShown);
    };

    const generatePassword = async (inputVariable,desiredLength) => {
      if (password.length < desiredLength / 2 && !fingerprint && !secretImage) {
        setError(`Please ensure all fields are correctly filled out.`);
        return;
      }

      // Prepare formData with the input and size
      const formData = new FormData();
      formData.append('input', password); // inputVariable should be defined or passed to the function
      formData.append('size', desiredLength); // desiredLength should be the size you want to pass

      try {
        const response = await axios.post('/process', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        if (response.data) {
          setGeneratedPassword(response.data.generatedPassword); // Assuming the server sends back an object with the generatedPassword field
          setError('');
        } else {
          setError('No password generated. Please try again.');
        }
      } catch (error) {
        setError('Failed to generate password. Please check your connection and try again.');
        console.error('Error during password generation:', error);
      }
    };



    return (
      <div style={{
        color: 'white',
        fontFamily: 'Arial',
        width: '350px',
        padding: '20px',
        margin: '0px auto 0px',
        borderRadius: '8px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh' // Make sure this div also takes full viewport height
      }}>
        <div style={{ background: '#071e26', padding: '40px', color: 'white', fontFamily: 'Arial', width: '350px', margin: '0px auto 20px', borderRadius: '8px', border: '2px solid #2596be' }}>
          <h2 style={{ textAlign: 'center', color: '#61dafb' }}>Password Generator</h2>
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '5px' }}>Enter or Generate Password:</label>
            <div style={{ background: 'black', padding: '10px', borderRadius: '5px', position: 'relative' }}>
              <input type={passwordShown ? 'text' : 'password'} value={password} onChange={handlePasswordChange} style={{ width: '100%', border: 'none', background: 'none', color: 'white', fontSize: '16px', letterSpacing: '2px' }} />
              <button onClick={togglePasswordVisibility} style={{ position: 'absolute', top: '10px', right: '10px', background: 'none', border: 'none', color: 'white' }}>
                {passwordShown ? '🙈' : '👁️'}
              </button>
            </div>
            {error && <div style={{ color: 'red', marginTop: '10px' }}>{error}</div>}
          </div>
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '5px' }}>Character Length: {desiredLength}</label>
            <input type="range" min="10" max="16" value={desiredLength} onChange={handleLengthChange} style={{ width: '100%' }} />
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <label style={{ cursor: 'pointer', marginRight: '10px' }}>
              Upload Fingerprint:
              <input
                type="file"
                onChange={(e) => {
                  setFingerprint(e.target.files[0]); // Set the file to state
                }}
                accept="image/*"
                style={{ display: 'none' }}
                id="fingerprintInput"
              />

              <span style={{ display: 'flex', alignItems: 'center', color: 'white', cursor: 'pointer', fontSize: '20px' }} onClick={(event) => {
                event.stopPropagation();
                document.getElementById('fingerprintInput').click();
              }}>
                🔍 {fingerprint ? fingerprint.name : ''}
              </span>
            </label>
          </div>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <label style={{ cursor: 'pointer', marginRight: '10px' }}>
              Upload Secret Image:
                  <input
                              type="file"
                              onChange={(e) => {
                                setSecretImage(e.target.files[0]); // Set the file to state
                                handleSecretImageUpload();         // Then call the function
                              }}
                              accept="image/*"
                              style={{ display: 'none' }}
                              id="SecretImageInput"
                            />
              <span style={{ display: 'flex', alignItems: 'center', color: 'white', cursor: 'pointer', fontSize: '20px' }} onClick={(event) => {
                event.stopPropagation();
                document.getElementById('secretImageInput').click();
              }}>
                🖼️ {secretImage ? secretImage.name : ''}
              </span>
            </label>
          </div>
        </div>
          <button onClick={generatePassword} disabled={!fingerprint || !secretImage || password.length < desiredLength / 2} style={{ width: '100%', padding: '10px', fontSize: '16px', cursor: 'pointer', backgroundColor: '#61dafb', border: 'none', borderRadius: '5px', marginBottom: '10px', opacity: (!fingerprint || !secretImage) ? 0.5 : 1 }}>
            Generate
          </button>
          {generatedPassword && (
            <div style={{ background: 'black', padding: '10px', borderRadius: '5px', position: 'relative' }}>
              <input style={{ width: '100%', border: 'none', background: 'none', color: 'white', fontSize: '16px', letterSpacing: '2px' }} type="text" value={generatedPassword} readOnly />
              <button onClick={() => navigator.clipboard.writeText(generatedPassword)} style={{ position: 'absolute', top: '10px', right: '10px', background: 'none', border: 'none', color: 'white' }}>📋</button>
            </div>
          )}
        </div>
      </div>
    );
  }

export default App;
