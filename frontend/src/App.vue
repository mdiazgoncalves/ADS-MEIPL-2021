<template>
  <div id="app">
    <Home/>
  </div>
</template>

<script>
import Home from "@/views/Home";

export default {
  name: 'App',
  components: {
    Home,
  }
}
</script>

<style>

@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;500&display=swap');

:root {
  --primary: #6866fa;
}

button {
  height: 40px;
  width: 160px;
  text-transform: uppercase;
}

button:hover {
  cursor: pointer;
}

a:hover {
  cursor: pointer;
  color: var(--primary);
}

button.primary {
  background-color: transparent;
  color: var(--primary);
  font-size: 14px;
  border: 1px solid var(--primary);
  border-radius: 4px;
}

button.primary:hover {
  background-color: var(--primary);
  color: white;
  border: none;
}

#app {
  font-family: Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  font-weight: 400;
}

h1, h2, h3, h4, h5, p, ul, ol, blockquote, figure, button, body {
  margin: 0;
  padding: 0;
}

a {
  color: black;
  text-decoration: none;
}

body {
  background-color: #F2F6F8;
}

h2 {
  font-family: Nunito, sans-serif;
  font-weight: 700;
  font-size: 16px;
}
</style>
