<template>
  <div class="card">
    <router-link :to="href">
      <header>
        <img :src="src" :alt="alt">
      </header>
      <h2>{{ title }}</h2>
    </router-link>
  </div>
</template>

<script>
export default {
  name: "HomeCard",
  props: {
    title: {type: String, required: true},
    href: {type: String, required: true},
    src: {type: String, required: true},
    alt: {type: String, required: true},
  },
}
</script>

<style scoped>
.card {
  background-color: white;
  box-shadow: rgba(0, 0, 0, 0.04) 0 3px 5px;
  border-radius: 12px;
  padding-bottom: 16px;
}

.card:hover {
  position: relative;
  bottom: 1px;
  box-shadow: rgba(149, 157, 165, 0.2) 0 8px 24px;
}

.card header img {
  display: block;
  width: 264px;
  height: 300px;
  margin: auto;
}

.card h2 {
  text-align: center;
  text-transform: uppercase;
}
</style>