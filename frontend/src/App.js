import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from 'react';

function App() {
  const [square, setSquare] = useState(null);
  useEffect(() => {
    fetch('/reactTest')
        .then(response => {
            console.log('Response received:', response);
            return response.json()
        })
        .then(data => {
          console.log('Data received:', data);
          setSquare(data);
        })
        .catch(e => console.error("Error : ",e))
  }, []);

  if (!square) {
    return <div>Loading...</div>;
  }

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React {square.color} & {square.size}
        </a>
      </header>
    </div>
  );
}

export default App;
