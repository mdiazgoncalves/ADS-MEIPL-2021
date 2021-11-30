<template>
  <nav>
    <ul>
      <li class="logo"><a>Knowledge Base</a></li>
    </ul>
    <ul>
      <li><a>Help</a></li>
      <li>
        <button class="primary">Login</button>
      </li>
    </ul>
  </nav>
</template>

<script>
export default {
  name: "HomeNavBar"
}
</script>

<style scoped>
nav {
  display: flex;
  justify-content: space-between;
  width: 70%;
  margin-top: 32px;
}

nav > ul {
  display: flex;
  align-items: center;
}

nav > ul > li {
  display: block;
  text-transform: uppercase;
  color: #494949;
}

nav > ul > li > a:not(:hover) {
  color: #494949;
}

nav > ul > li:not(:last-child) {
  padding-right: 40px;
}

nav .logo, nav .logo a {
  font-family: Nunito, sans-serif;
  font-weight: 700;
  font-size: 20px;
  color: black !important;
}

</style>