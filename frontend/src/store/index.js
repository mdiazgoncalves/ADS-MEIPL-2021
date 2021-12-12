import { createStore } from 'vuex'
import loadingModule from'./modules/loading';
import sessionModule from './modules/session';

export default createStore({
    modules: {
        loadingModule,
        sessionModule,
    }
})